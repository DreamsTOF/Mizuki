import { join } from 'path';
export default {
    requestLibPath: "@/request",
    // 关键修复：使用 process.cwd() 获取项目根目录，拼出绝对路径
    // 最终路径会变成：C:\code\song-list\songs-frontend\openapi.json
    schemaPath: join(process.cwd(), 'openapi.json'),
    // 同样为了稳妥，输出目录也用绝对路径锁死
    serversPath: join(process.cwd(), 'src/api/generated'),
};
