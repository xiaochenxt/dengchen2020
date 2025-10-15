SELECT
    castsource::regtype AS 源类型,
    casttarget::regtype AS 目标类型,
    castfunc::regproc AS 转换函数,
    castcontext AS 转换场景  -- a=ASSIGNMENT（赋值时）, i=IMPLICIT（隐式）, e-显式转换
FROM pg_cast
WHERE
    castsource = 'varchar'::regtype -- 源类型
  AND casttarget = 'jsonb'::regtype; -- 目标类型