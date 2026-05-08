import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
const GENERATED_ROOT = path.resolve(__dirname, '../api/generated');
const API_JSON_PATH = path.resolve(__dirname, '../../api.json');

/**
/**
 * 子模块名称映射配置
 * 键：后端子模块中文名称（如 "歌曲信息表"）
 * 值：前端英文名称（PascalCase）
 */
const SUB_MODULE_NAME_MAP = {
  '歌曲标签关联表': 'SongLabel',
  '歌曲信息表': 'Song',
  '歌曲点击统计表': 'SongClickStat',
  '用户信息表': 'User',
  '用户信息': 'User',
  '登录注册': 'Auth',
  '秘钥握手': 'Crypto',
};

/**
 * 独立模块名称映射配置（没有子模块的模块）
 * 键：后端目录名（由 orval 生成，可能是中文或英文）
 * 值：前端英文名称（PascalCase）
 */
const STANDALONE_MODULE_NAME_MAP = {
  // 英文目录名映射
  'playlist': 'Playlist',
  'auth': 'Auth',
  'label': 'Label',
  'public': 'Public',
  '用户信息': 'User',
  '登录注册': 'Auth',
  // 中文目录名映射（orval 生成的中文 tag 目录）
  '歌单管理': 'Playlist',
  '认证管理': 'Auth',
  '标签管理': 'Label',
  '秘钥握手': 'Crypto',
  '公开信息': 'Public',
};

/**
 * 从 api.json 读取 tags，构建完整的 tag 名称映射
 * 格式: { '模块名/子模块名': 'PascalCase名称', ... }
 * @returns {Map<string, string>} tag名称 -> PascalCase名称 的映射
 */
function buildTagNameMapFromApiJson() {
  try {
    const apiJson = JSON.parse(fs.readFileSync(API_JSON_PATH, 'utf-8'));
    const tags = apiJson.tags || [];
    const tagMap = new Map();

    tags.forEach((tag) => {
      const tagName = tag.name;
      // 只处理子模块标签（包含 '/'）
      if (tagName.includes('/')) {
        const [, subModuleName] = tagName.split('/');
        // 使用 SUB_MODULE_NAME_MAP 映射，如果没有则使用 toPascalCase 转换
        const pascalName = SUB_MODULE_NAME_MAP[subModuleName] || toPascalCase(subModuleName);
        tagMap.set(tagName, pascalName);
      }
    });

    return tagMap;
  } catch (error) {
    console.error('❌ 读取 api.json 失败:', error);
    return new Map();
  }
}

function toPascalCase(str) {
  return str
    .replace(/Controller$/i, '')
    .split('-')
    .map(part => part.charAt(0).toUpperCase() + part.slice(1))
    .join('');
}

function toCamelCase(str) {
  return str
    .split('-')
    .map((part, index) =>
      index === 0 ? part.charAt(0).toLowerCase() + part.slice(1) : part.charAt(0).toUpperCase() + part.slice(1)
    )
    .join('');
}

/**
 * 获取子模块名称
 * 优先从 api.json 构建的映射中查找，其次使用转换函数
 */
function getSubModuleName(dirName, tagNameMap) {
  // 优先从 api.json 构建的映射中查找
  if (tagNameMap.has(dirName)) {
    return tagNameMap.get(dirName);
  }

  // 回退到 SUB_MODULE_NAME_MAP 查找（兼容旧逻辑）
  for (const [key, value] of Object.entries(SUB_MODULE_NAME_MAP)) {
    if (dirName.includes(key)) {
      return value;
    }
  }

  // 最后使用默认转换
  return toPascalCase(dirName);
}

/**
 * 获取独立模块名称
 */
function getStandaloneModuleName(dirName) {
  // 从 STANDALONE_MODULE_NAME_MAP 查找
  if (STANDALONE_MODULE_NAME_MAP[dirName]) {
    return STANDALONE_MODULE_NAME_MAP[dirName];
  }

  // 使用默认转换
  return toPascalCase(dirName);
}

function processApiFile(content) {
  content = content.replace(/\s*responseType:\s*'blob',?/g, '');
  content = content.replace(/customInstance<Blob>/g, 'customInstance<unknown>');
  // 修复 request-adapter 导入路径：从 '../../../request-adapter' 改为 '../../request-adapter'
  content = content.replace(/'\.\.\/\.\.\/\.\.\/request-adapter'/g, "'../../request-adapter'");
  // 修复 model 导入路径：从 '../../model' 改为 '../model'
  content = content.replace(/'\.\.\/\.\.\/model'/g, "'../model'");
  content = content.replace(/from\s*'\.\.\/\.\.\/model'/g, "from '../model'");

  content = transformToDirectExports(content);

  return content;
}

function transformToDirectExports(content) {
  const getMatch = content.match(/export\s+const\s+get\s*=\s*\(\s*\)\s*=>\s*\{([\s\S]*?)\n\s*return\s+\{[^}]+\};?\s*\}/);

  if (!getMatch) {
    return content;
  }

  const functionBody = getMatch[1];

  const lines = functionBody.split('\n');
  const functions = [];
  let currentFunc = null;
  let braceCount = 0;
  let inFunction = false;

  for (let i = 0; i < lines.length; i++) {
    const line = lines[i];
    const trimmedLine = line.trim();

    if (trimmedLine.startsWith('/**')) {
      if (currentFunc && inFunction) {
        functions.push(currentFunc);
      }
      currentFunc = { comment: '', body: '' };
      braceCount = 0;
      inFunction = false;
    }

    if (currentFunc) {
      if (trimmedLine.startsWith('/**') || trimmedLine.startsWith('*') || trimmedLine.endsWith('*/')) {
        currentFunc.comment += line + '\n';
        continue;
      }

      if (trimmedLine.startsWith('const ') || trimmedLine.match(/^[a-z]/i)) {
        inFunction = true;
      }

      if (inFunction) {
        currentFunc.body += line + '\n';
        braceCount += (line.match(/\{/g) || []).length;
        braceCount -= (line.match(/\}/g) || []).length;

        if (braceCount === 0 && line.includes('}')) {
          functions.push(currentFunc);
          currentFunc = null;
          inFunction = false;
        }
      }
    }
  }

  if (currentFunc && inFunction) {
    functions.push(currentFunc);
  }

  let newContent = content.substring(0, getMatch.index);

  for (const func of functions) {
    const funcMatch = func.body.match(/(?:const\s+)?([a-zA-Z][a-zA-Z0-9-]*)\s*=\s*\(([^)]*)\)\s*(?::\s*[^=]+)?\s*=>?\s*\{([\s\S]*)\}/);
    if (funcMatch) {
      let funcName = funcMatch[1];
      const params = funcMatch[2].trim();
      const body = funcMatch[3].trim();

      funcName = toCamelCase(funcName);

      const customInstanceMatch = body.match(/return\s+customInstance<[^>]+>\(\s*\{[\s\S]*?\},?\s*\);?/);
      if (customInstanceMatch) {
        let customInstanceCall = customInstanceMatch[0];

        let comment = func.comment.trim();
        if (comment && !comment.startsWith('/**')) {
          comment = '/**\n' + comment;
        }
        if (comment && !comment.endsWith('*/')) {
          comment = comment + ' */';
        }
        if (comment) {
          newContent += `${comment}\n`;
        }
        newContent += `export const ${funcName} = (${params}) => {\n  ${customInstanceCall}\n}\n\n`;
      }
    }
  }

  const afterGet = content.substring(getMatch.index + getMatch[0].length);

  let typeDefinitions = afterGet;

  typeDefinitions = typeDefinitions.replace(
    /export type (\w+)Result = NonNullable<Awaited<ReturnType<ReturnType<typeof get>\['([a-zA-Z0-9-]+)'\]>>>/g,
    (match, typeName, originalFuncName) => {
      const newFuncName = toCamelCase(originalFuncName);
      return `export type ${typeName}Result = NonNullable<Awaited<ReturnType<typeof ${newFuncName}>>>`;
    }
  );

  typeDefinitions = typeDefinitions.replace(/^;\s*\n/gm, '');

  newContent += typeDefinitions;

  return newContent;
}

function processGenerated() {
  if (!fs.existsSync(GENERATED_ROOT)) {
    console.log('⚠️  generated 目录不存在，跳过处理');
    return;
  }

  // 从 api.json 构建 tag 名称映射
  const tagNameMap = buildTagNameMapFromApiJson();
  console.log('📋 从 api.json 读取的模块映射:');
  tagNameMap.forEach((value, key) => {
    console.log(`   ${key} -> ${value}`);
  });

  const modules = fs.readdirSync(GENERATED_ROOT).filter(m =>
    fs.statSync(path.join(GENERATED_ROOT, m)).isDirectory()
  );

  let processedCount = 0;
  let errorCount = 0;

  modules.forEach(moduleName => {
    const modulePath = path.join(GENERATED_ROOT, moduleName);
    const items = fs.readdirSync(modulePath);

    items.forEach(item => {
      const itemPath = path.join(modulePath, item);

      if (fs.statSync(itemPath).isDirectory() && item !== 'model') {
        // 构建完整的 tag 名称（模块名/子模块名）来查找映射
        // 需要从原始 tag 名称中找到匹配的
        let subModuleName = null;

        // 尝试在 tagNameMap 中查找匹配的 tag
        for (const [tagName, pascalName] of tagNameMap.entries()) {
          // 将 tag 中的 / 替换为 - 来匹配目录名（Orval 生成的目录名格式）
          const normalizedTag = tagName.replace(/\//g, '-');
          if (item === normalizedTag || normalizedTag.includes(item) || item.includes(normalizedTag)) {
            subModuleName = pascalName;
            break;
          }
        }

        // 如果没找到，检查是否是独立模块的中文目录名
        if (!subModuleName) {
          if (STANDALONE_MODULE_NAME_MAP[item]) {
            subModuleName = STANDALONE_MODULE_NAME_MAP[item];
          } else {
            subModuleName = toPascalCase(item);
          }
        }

        const newFileName = `${subModuleName}.ts`;
        const newPath = path.join(modulePath, newFileName);

        const subFiles = fs.readdirSync(itemPath).filter(f => f.endsWith('.ts'));

        subFiles.forEach(file => {
          if (file.includes('index')) return;

          try {
            const sourcePath = path.join(itemPath, file);
            let content = fs.readFileSync(sourcePath, 'utf-8');

            content = processApiFile(content);

            fs.writeFileSync(newPath, content, 'utf-8');
            processedCount++;

            console.log(`✅ 处理完成: ${moduleName}/${newFileName}`);
          } catch (error) {
            errorCount++;
            console.error(`❌ 处理失败: ${moduleName}/${file}`, error);
          }
        });

        fs.rmSync(itemPath, { recursive: true, force: true });
      }
    });

    const rootApiFile = path.join(modulePath, 'api.ts');
    if (fs.existsSync(rootApiFile)) {
      fs.unlinkSync(rootApiFile);
    }
  });

  console.log(`\n📊 处理统计:`);
  console.log(`   - 成功: ${processedCount} 个文件`);
  console.log(`   - 失败: ${errorCount} 个文件`);
  console.log(`✨ Generated 文件处理完成`);
}

processGenerated();
