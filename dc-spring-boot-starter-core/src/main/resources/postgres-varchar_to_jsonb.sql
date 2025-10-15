-- 如果之前存在varchar转jsonb的转换规则就删除
DROP CAST IF EXISTS (varchar AS jsonb);
-- 如果之前存在varchar转jsonb的转换函数就删除
DROP FUNCTION IF EXISTS varchar_to_jsonb(varchar);

-- 创建varchar转jsonb的转换规则
create or replace function varchar_to_jsonb(varchar) returns jsonb as $$
SELECT jsonb_in($1::cstring);$$ language sql strict immutable;
-- 关联转换规则到varchar_to_jsonb转换函数（仅赋值场景生效，例如insert、update）
create cast (varchar as jsonb) with function varchar_to_jsonb(varchar) as assignment;