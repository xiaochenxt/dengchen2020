-- 如果之前存在varchar转bigint的转换规则就删除
DROP CAST IF EXISTS (varchar AS bigint);
-- 如果之前存在varchar转bigint的转换函数就删除
DROP FUNCTION IF EXISTS varchar_to_bigint(varchar);

-- 创建varchar转bigint的转换规则
create or replace function varchar_to_bigint(varchar) returns bigint as $$
SELECT int8in($1::cstring);$$ language sql strict immutable;
-- 关联转换规则到varchar_to_bigint转换函数（任意场景隐式触发）
create cast (varchar as bigint) with function varchar_to_bigint(varchar) as implicit;