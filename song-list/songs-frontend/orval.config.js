import { defineConfig } from 'orval';
import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

/**
 * 模块名称映射配置
 * 键：后端模块名称（中文）
 * 值：前端模块名称（英文小写）
 */
const MODULE_NAME_MAP = {
  '歌单管理': 'playlist',
  '认证管理': 'auth',
  '标签管理': 'label',
  '歌曲': 'song',
  '认证': 'auth',
  '公开信息': 'public',
};

/**
 * 子模块名称映射配置
 * 键：后端子模块名称（中文）
 * 值：前端子模块名称（PascalCase）
 */
const SUB_MODULE_NAME_MAP = {
  '歌曲标签关联表': 'SongLabel',
  '歌曲信息表': 'Song',
  '歌曲点击统计表': 'SongClickStat',
  '用户信息表': 'User',
  '公开信息': 'Public',
};

/**
 * 独立模块配置（没有子模块的模块）
 * 键：后端模块名称
 * 值：前端 Controller 名称
 */
const STANDALONE_MODULE_MAP = {
  '歌单管理': 'Playlist',
  '认证管理': 'Auth',
  '标签管理': 'Label',
  '公开信息': 'Public',
};

/**
 * 需要单独生成文件的子模块配置
 * 键：后端子模块 tag 名称
 * 值：{ moduleName: 前端模块名, fileName: 生成文件名 }
 */
const SEPARATE_SUB_MODULE_MAP = {
  '认证/用户信息表': { moduleName: 'auth', fileName: 'User' },
};

/**
 * 统一的 model 输出目录
 */
const SHARED_MODEL_DIR = './src/api/generated/model';

/**
 * 从 api.json 解析 tags，自动构建模块和子模块映射
 * @param {string} apiJsonPath - api.json 文件路径
 * @returns {Object} - { moduleMap: Map, standaloneModules: Set, separateSubModules: Map }
 */
function parseApiTags(apiJsonPath) {
  try {
    const apiJson = JSON.parse(fs.readFileSync(apiJsonPath, 'utf-8'));
    const tags = apiJson.tags || [];

    if (tags.length === 0) {
      console.warn('⚠️  api.json 中没有定义 tags');
      return { moduleMap: new Map(), standaloneModules: new Set(), separateSubModules: new Map() };
    }

    const moduleMap = new Map();
    const standaloneModules = new Set();
    const separateSubModules = new Map();

    // 首先收集所有子模块（带 / 的 tag）
    tags.forEach((tag) => {
      const tagName = tag.name;
      if (tagName.includes('/')) {
        const [moduleName] = tagName.split('/');
      }
    });

    // 然后处理所有 tags
    tags.forEach((tag) => {
      const tagName = tag.name;

      if (tagName.includes('/')) {
        // 检查是否需要单独生成文件
        if (SEPARATE_SUB_MODULE_MAP[tagName]) {
          const config = SEPARATE_SUB_MODULE_MAP[tagName];
          separateSubModules.set(tagName, config);
        } else {
          // 处理普通子模块标签（如 "歌曲/歌曲信息表"）
          const [moduleName] = tagName.split('/');

          if (!moduleMap.has(moduleName)) {
            moduleMap.set(moduleName, []);
          }
          moduleMap.get(moduleName).push(tagName);
        }
      } else if (STANDALONE_MODULE_MAP[tagName]) {
        // 处理独立模块（如 "歌单管理"）- 只有配置了映射的才处理
        standaloneModules.add(tagName);
      }
    });

    return { moduleMap, standaloneModules, separateSubModules };
  } catch (error) {
    console.error('❌ 读取 api.json 失败:', error);
    return { moduleMap: new Map(), standaloneModules: new Set(), separateSubModules: new Map() };
  }
}

/**
 * 获取前端模块名称
 * @param {string} backendModuleName - 后端模块名称（中文）
 * @returns {string} - 前端模块名称（英文小写）
 */
function getFrontendModuleName(backendModuleName) {
  return MODULE_NAME_MAP[backendModuleName] ||
         backendModuleName.toLowerCase().replace(/[^a-z0-9]/g, '');
}

/**
 * 创建 orval 配置的辅助函数
 */
function createOrvalConfig(tags, outputDir) {
  return {
    input: {
      target: './api.json',
      filters: {
        tags: Array.isArray(tags) ? tags : [tags],
      },
    },
    output: {
      mode: 'tags-split',
      target: `./src/api/generated/${outputDir}/api.ts`,
      schemas: SHARED_MODEL_DIR,  // 使用统一的 model 目录
      client: 'axios',
      override: {
        mutator: {
          path: './src/api/request-adapter.ts',
          name: 'customInstance',
        },
        operationName: (operation, route, verb) => {
          const pathParts = route.split('/').filter(p => p && !/[{}$]/.test(p));
          return pathParts.map((p, i) =>
            i === 0
              ? verb + p.charAt(0).toUpperCase() + p.slice(1)
              : p.charAt(0).toUpperCase() + p.slice(1)
          ).join('');
        },
      },
    },
  };
}

/**
 * 生成 Orval 配置
 * 自动从 api.json 读取模块结构，无需硬编码 SUB_MODULE_MAP
 */
function generateConfigs() {
  const apiJsonPath = path.resolve(__dirname, './api.json');
  const { moduleMap, standaloneModules, separateSubModules } = parseApiTags(apiJsonPath);
  const configs = {};

  // 处理需要单独生成文件的子模块
  separateSubModules.forEach((config, tagName) => {
    const configKey = `${config.moduleName}-${config.fileName}`.toLowerCase();
    configs[configKey] = createOrvalConfig(tagName, config.moduleName);
  });

  // 处理有子模块的模块
  moduleMap.forEach((subTags, backendModuleName) => {
    const frontendModuleName = getFrontendModuleName(backendModuleName);

    configs[frontendModuleName] = createOrvalConfig(subTags, frontendModuleName);
  });

  // 处理独立模块（没有子模块）
  standaloneModules.forEach((backendModuleName) => {
    const frontendModuleName = getFrontendModuleName(backendModuleName);

    configs[frontendModuleName] = createOrvalConfig(backendModuleName, frontendModuleName);
  });

  return configs;
}

export default defineConfig(generateConfigs());
