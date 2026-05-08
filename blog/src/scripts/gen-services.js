import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
const GENERATED_ROOT = path.resolve(__dirname, '../api/generated');
const SERVICES_ROOT = path.resolve(__dirname, '../api/real');
const INDEX_FILE = path.resolve(__dirname, '../api/index.ts');

/**
 * 从生成的 API 文件中提取导出的函数信息
 */
function extractExportedFunctions(content) {
  const functions = [];

  const exportMatches = content.matchAll(/export\s+const\s+(\w+)\s*=\s*\(([^)]*)\)/g);
  for (const match of exportMatches) {
    const [, name, params] = match;
    if (!name.endsWith('Result')) {
      const methodMatch = content.match(new RegExp(`export const ${name}[\\s\\S]*?method:\\s*['"](\\w+)['"]`));
      const method = methodMatch ? methodMatch[1] : 'GET';
      const urlMatch = content.match(new RegExp(`export const ${name}[\\s\\S]*?url:\\s*\`([^\`]+)\``));
      const url = urlMatch ? urlMatch[1] : '';
      // 提取返回类型
      const returnTypeMatch = content.match(new RegExp(`export const ${name}[\\s\\S]*?customInstance<([^>]+)>`));
      const returnType = returnTypeMatch ? returnTypeMatch[1].trim() : 'unknown';
      functions.push({
        name,
        params: params.trim(),
        method: method.toUpperCase(),
        url,
        returnType,
      });
    }
  }

  return functions;
}

/**
 * 提取函数参数中使用的类型
 */
function extractUsedTypes(functions, content) {
  const types = new Set();

  for (const fn of functions) {
    const params = fn.params;
    const typeMatches = params.matchAll(/:\s*([A-Z][a-zA-Z0-9]*(?:<[^>]+>)?(?:\[\])?)/g);
    for (const match of typeMatches) {
      const typeName = match[1];
      if (!typeName.startsWith('Base') && !typeName.includes('Result')) {
        types.add(typeName.replace('[]', '').replace(/<.*>/, ''));
      }
    }
  }

  return Array.from(types);
}

/**
 * 解析 URL 中的路径参数
 * 例如: '/songs/song/detail/${id}' -> ['id']
 */
function extractPathParams(url) {
  const params = [];
  const matches = url.matchAll(/\$\{(\w+)\}/g);
  for (const match of matches) {
    params.push(match[1]);
  }
  return params;
}

/**
 * 清理参数名，移除可选标记 ?
 * 例如: "params?" -> "params"
 */
function cleanParamName(paramName) {
  return paramName.replace(/\?$/, '');
}

/**
 * 生成 QueryKey，处理 URL 中的路径参数
 */
function generateQueryKey(url, paramNames) {
  const pathParams = extractPathParams(url);
  // 清理参数名（移除 ?）
  const cleanedParamNames = paramNames.map(cleanParamName);
  const queryParams = cleanedParamNames.filter(p => !pathParams.includes(p));

  if (pathParams.length > 0) {
    if (queryParams.length > 0) {
      return `[\`${url}\`, ${queryParams.join(', ')}]`;
    }
    return `[\`${url}\`]`;
  }
  return `['${url}'${queryParams.length > 0 ? `, ${queryParams.join(', ')}` : ''}]`;
}

/**
 * 将方法名转换为 hook 名称
 * 例如: getAuthMe -> useGetAuthMe, postAuthLogin -> usePostAuthLogin
 */
function toHookName(methodName) {
  return `use${methodName.charAt(0).toUpperCase()}${methodName.slice(1)}`;
}

/**
 * 提取参数类型信息
 * 例如: "id: string, params?: QueryParams" -> { name: 'id', type: 'string' }[]
 * 注意：可选参数名中的 ? 会被移除
 */
function parseParamTypes(params) {
  if (!params) return [];
  return params.split(',').map(p => {
    const [namePart, ...typeParts] = p.split(':');
    const name = namePart.trim().replace(/\?$/, ''); // 移除可选标记 ?
    const type = typeParts.join(':').trim(); // 处理类型中可能包含 : 的情况（如 import("...").Type）
    return { name, type };
  }).filter(p => p.name && p.type);
}

/**
 * 清理参数字符串，移除末尾多余的逗号
 * 例如: "id: string," -> "id: string"
 */
function cleanParams(params) {
  if (!params) return '';
  // 移除末尾的逗号和空白
  return params.replace(/,\s*$/, '').trim();
}

/**
 * 生成 Controller 类模板
 */
function generateControllerTemplate(moduleName, fileName, className, functions, usedTypes) {
  // 使用统一的 model 目录路径
  const typeImports = usedTypes.length > 0
    ? `import type { ${usedTypes.join(', ')} } from '../../generated/model';\n`
    : '';

  // 收集所有需要的返回类型用于 import
  const returnTypes = functions.map(fn => fn.returnType).filter(t => t && t !== 'unknown');
  const allTypes = [...new Set([...usedTypes, ...returnTypes])];
  const finalTypeImports = allTypes.length > 0
    ? `import type { ${allTypes.join(', ')} } from '../../generated/model';\n`
    : '';

  // 生成方法代理和 Query/Mutation 辅助方法
  const methodSections = functions.map(fn => {
    const lines = [];
    const url = fn.url || `/${fileName.toLowerCase()}`;
    const hookName = toHookName(fn.name);

    // 解析参数（清理末尾逗号）
    const cleanedParams = cleanParams(fn.params);
    const paramTypes = parseParamTypes(cleanedParams);
    const paramNames = paramTypes.map(p => p.name);

    // 获取参数类型（用于 Mutation 泛型）
    const variablesType = paramTypes.length > 0 ? paramTypes[0].type : 'void';
    const returnType = fn.returnType || 'unknown';

    // 1. 直接代理 API 方法
    lines.push(`  static ${fn.name} = Api.${fn.name};`);

    // 2. 根据 HTTP 方法生成对应的 Query/Mutation 辅助方法
    if (fn.method === 'GET') {
      // GET 方法：生成 QueryKey 和 QueryFn
      const queryKey = generateQueryKey(url, paramNames);

      const queryKeyParams = cleanedParams || '';
      lines.push(`  static ${fn.name}QueryKey = (${queryKeyParams}) => ${queryKey};`);

      const queryFnParams = cleanedParams || '';
      const apiCall = paramNames.length > 0
        ? `Api.${fn.name}(${paramNames.join(', ')})`
        : `Api.${fn.name}()`;
      lines.push(`  static ${fn.name}QueryFn = (${queryFnParams}) => () => ${apiCall};`);

      // Vue Composition API hook for GET - 使用正确的泛型类型
      const queryOptionsType = `UseQueryOptions<${returnType}>`;
      if (cleanedParams) {
        lines.push(`  static ${hookName} = (${cleanedParams}, options?: ${queryOptionsType}) => {`);
        lines.push(`    return useQuery({`);
        lines.push(`      queryKey: ${className}.${fn.name}QueryKey(${paramNames.join(', ')}),`);
        lines.push(`      queryFn: ${className}.${fn.name}QueryFn(${paramNames.join(', ')}),`);
        lines.push(`      ...options,`);
        lines.push(`    });`);
        lines.push(`  };`);
      } else {
        lines.push(`  static ${hookName} = (options?: ${queryOptionsType}) => {`);
        lines.push(`    return useQuery({`);
        lines.push(`      queryKey: ${className}.${fn.name}QueryKey(),`);
        lines.push(`      queryFn: ${className}.${fn.name}QueryFn(),`);
        lines.push(`      ...options,`);
        lines.push(`    });`);
        lines.push(`  };`);
      }
    } else {
      // POST/PUT/DELETE 方法：生成 MutationFn
      const mutationFnParams = cleanedParams || '';

      // 处理多参数情况：如果有多于1个参数，需要包装成对象
      const hasMultipleParams = paramTypes.length > 1;
      const variablesType = hasMultipleParams
        ? `{ ${paramTypes.map(p => `${p.name}: ${p.type}`).join(', ')} }`
        : (paramTypes.length === 1 ? paramTypes[0].type : 'void');

      if (hasMultipleParams) {
        // 多参数：生成包装对象的 MutationFn
        const destructuredParams = paramTypes.map(p => p.name).join(', ');
        lines.push(`  static ${fn.name}MutationFn = (variables: { ${paramTypes.map(p => `${p.name}: ${p.type}`).join(', ')} }) => {`);
        lines.push(`    const { ${destructuredParams} } = variables;`);
        lines.push(`    return Api.${fn.name}(${destructuredParams});`);
        lines.push(`  };`);
      } else {
        // 单参数或无参数：保持原样
        const apiCall = paramNames.length > 0
          ? `Api.${fn.name}(${paramNames.join(', ')})`
          : `Api.${fn.name}()`;
        lines.push(`  static ${fn.name}MutationFn = (${mutationFnParams}) => ${apiCall};`);
      }

      // Vue Composition API hook for Mutation - 使用正确的泛型类型
      const mutationOptionsType = paramTypes.length > 0
        ? `UseMutationOptions<${returnType}, Error, ${variablesType}>`
        : `UseMutationOptions<${returnType}>`;
      lines.push(`  static ${hookName} = (options?: ${mutationOptionsType}) => {`);
      lines.push(`    return useMutation({`);
      lines.push(`      mutationFn: ${className}.${fn.name}MutationFn,`);
      lines.push(`      ...options,`);
      lines.push(`    });`);
      lines.push(`  };`);
    }

    return lines.join('\n');
  });

  const methods = methodSections.join('\n\n');

  return `import { useQuery, useMutation } from '@tanstack/vue-query';
import type { UseQueryOptions, UseMutationOptions } from '@tanstack/vue-query';
import * as Api from '../../generated/${moduleName}/${fileName}';
${finalTypeImports}
/**
 * ${className.replace('Controller', '')} 业务控制器
 * @description 自动生成，支持 TanStack Query
 * @see {@link ../../generated/${moduleName}/${fileName}} 生成的 API 函数
 */
export class ${className} {
${methods}
}
`;
}

function generateIndexContent(exports) {
  const header = `// 🛡️ 自动生成的 API 业务层聚合入口
// ⚠️ 此文件由脚本自动生成，请勿手动修改

`;
  return header + exports.join('\n');
}

function generateServices() {
  if (!fs.existsSync(GENERATED_ROOT)) {
    console.log('⚠️  generated 目录不存在，跳过处理');
    return;
  }

  const exports = [];
  let createdCount = 0;
  let skippedCount = 0;

  const modules = fs.readdirSync(GENERATED_ROOT).filter(m =>
    fs.statSync(path.join(GENERATED_ROOT, m)).isDirectory()
  );

  modules.forEach(moduleName => {
    // 跳过 model 目录 - 这是共享的类型定义目录，不生成 Controller
    if (moduleName === 'model') {
      return;
    }

    const modulePath = path.join(GENERATED_ROOT, moduleName);
    const serviceModulePath = path.join(SERVICES_ROOT, moduleName);

    if (!fs.existsSync(serviceModulePath)) {
      fs.mkdirSync(serviceModulePath, { recursive: true });
    }

    // 只处理 .ts 文件，排除 api.ts 和 model 目录
    const genFiles = fs.readdirSync(modulePath).filter(f => {
      const filePath = path.join(modulePath, f);
      const isDirectory = fs.statSync(filePath).isDirectory();
      return f.endsWith('.ts') && f !== 'api.ts' && !isDirectory;
    });

    genFiles.forEach(file => {
      const fileName = path.basename(file, '.ts');
      const className = `${fileName}Controller`;
      const serviceFilePath = path.join(serviceModulePath, `${className}.ts`);

      const genFilePath = path.join(modulePath, file);
      const content = fs.readFileSync(genFilePath, 'utf-8');
      const functions = extractExportedFunctions(content);
      const usedTypes = extractUsedTypes(functions, content);

      if (fs.existsSync(serviceFilePath)) {
        skippedCount++;
        console.log(`⏭️  跳过已存在: ${moduleName}/${className}.ts`);
      } else {
        const template = generateControllerTemplate(moduleName, fileName, className, functions, usedTypes);
        fs.writeFileSync(serviceFilePath, template, 'utf-8');
        createdCount++;
        console.log(`✅ 创建完成: ${moduleName}/${className}.ts`);
      }

      exports.push(`export * from './real/${moduleName}/${className}';`);
    });
  });

  const indexContent = generateIndexContent(exports);
  fs.writeFileSync(INDEX_FILE, indexContent, 'utf-8');

  console.log(`\n📊 生成统计:`);
  console.log(`   - 新建: ${createdCount} 个文件`);
  console.log(`   - 跳过: ${skippedCount} 个文件`);
  console.log(`🚀 业务类生成完成`);
}

generateServices();
