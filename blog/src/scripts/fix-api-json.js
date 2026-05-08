#!/usr/bin/env node
/**
 * 修复 Apifox 生成的 api.json 中的 OpenAPI 3.0 规范兼容性问题
 *
 * 主要修复：
 * 1. 将 type: "" 替换为 type: "string"
 * 2. 将 examples: [...] 替换为 example: ...
 * 3. 将 type: "null" 替换为 nullable: true
 */

import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
const API_JSON_PATH = path.resolve(__dirname, '../../api.json');

function fixApiJson() {
  console.log('🔧 开始修复 api.json...');

  let content = fs.readFileSync(API_JSON_PATH, 'utf-8');
  let apiJson;

  try {
    apiJson = JSON.parse(content);
  } catch (error) {
    console.error('❌ 解析 api.json 失败:', error.message);
    process.exit(1);
  }

  let fixCount = 0;

  // 递归修复函数
  function fixObject(obj, path = '') {
    if (typeof obj !== 'object' || obj === null) return;

    if (Array.isArray(obj)) {
      obj.forEach((item, index) => fixObject(item, `${path}[${index}]`));
      return;
    }

    // 修复 1: type: "" -> type: "string"
    if (obj.type === '') {
      obj.type = 'string';
      fixCount++;
      console.log(`  ✅ 修复空 type: ${path || 'root'}`);
    }

    // 修复 2: type: "null" -> 移除 type，添加 nullable: true
    if (obj.type === 'null') {
      delete obj.type;
      obj.nullable = true;
      fixCount++;
      console.log(`  ✅ 修复 null type: ${path || 'root'}`);
    }

    // 修复 3: examples: [...] -> example: ...
    if (obj.examples !== undefined && Array.isArray(obj.examples)) {
      // 取第一个 example
      obj.example = obj.examples[0];
      delete obj.examples;
      fixCount++;
      console.log(`  ✅ 修复 examples: ${path || 'root'}`);
    }

    // 递归处理子属性
    for (const key of Object.keys(obj)) {
      fixObject(obj[key], `${path}.${key}`);
    }
  }

  fixObject(apiJson);

  // 写回文件
  fs.writeFileSync(API_JSON_PATH, JSON.stringify(apiJson, null, 2), 'utf-8');

  console.log(`\n📊 修复完成:`);
  console.log(`   - 共修复 ${fixCount} 个问题`);
  console.log(`✨ api.json 已更新`);
}

fixApiJson();
