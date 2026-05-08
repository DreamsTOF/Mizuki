#!/usr/bin/env node
import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
const API_JSON_PATH = path.resolve(__dirname, '../../api.json');

const DEFAULT_API_VERSION = process.env.API_DOWNLOAD_VERSION || 'openapi30';
const DEFAULT_API_URL = process.env.API_DOWNLOAD_URL || `http://127.0.0.1:4523/export/openapi/3?version=${DEFAULT_API_VERSION}`;

function printUsage() {
  console.log(`
用法:
  node src/scripts/download-api.js [URL]

说明:
  从 Apifox OpenAPI URL 下载接口定义文件并保存为 api.json。

参数:
  URL  Apifox 导出 URL（可选）
       不传则使用默认 URL: ${DEFAULT_API_URL}
       也可通过环境变量 API_DOWNLOAD_URL 设置

示例:
  node src/scripts/download-api.js
  node src/scripts/download-api.js http://127.0.0.1:4523/export/openapi/3?version=openapi30
  API_DOWNLOAD_URL=http://localhost:4523/export/openapi/3 node src/scripts/download-api.js
`);
}

async function downloadApi(url) {
  console.log(`⬇️  正在从 Apifox 下载接口定义...`);
  console.log(`   URL: ${url}`);

  try {
    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), 30000);

    const response = await fetch(url, {
      signal: controller.signal,
      headers: {
        'Accept': 'application/json',
      },
    });

    clearTimeout(timeoutId);

    if (!response.ok) {
      throw new Error(`HTTP ${response.status}: ${response.statusText}`);
    }

    const data = await response.json();

    fs.writeFileSync(API_JSON_PATH, JSON.stringify(data, null, 2), 'utf-8');

    const stats = fs.statSync(API_JSON_PATH);
    const fileSizeKB = (stats.size / 1024).toFixed(1);

    console.log(`✅ 下载成功: ${API_JSON_PATH}`);
    console.log(`   文件大小: ${fileSizeKB} KB`);
    console.log(`   接口数量: ${data.paths ? Object.keys(data.paths).length : '未知'}`);
  } catch (error) {
    if (error.name === 'AbortError') {
      console.error('❌ 下载超时（30秒），请检查 Apifox 是否正在运行');
    } else {
      console.error('❌ 下载失败:', error.message);
    }
    process.exit(1);
  }
}

function main() {
  const args = process.argv.slice(2);

  if (args.includes('--help') || args.includes('-h')) {
    printUsage();
    process.exit(0);
  }

  const url = args[0] || process.env.API_DOWNLOAD_URL || DEFAULT_API_URL;

  if (!url) {
    console.error('❌ 未提供 URL');
    printUsage();
    process.exit(1);
  }

  downloadApi(url).catch((error) => {
    console.error('❌ 下载失败:', error.message);
    process.exit(1);
  });
}

main();
