import re

with open('src/main/java/com/classification/domain_system/repository/CustomRecordRepositoryImpl.java', 'r', encoding='utf-8') as f:
    content = f.read()

# Patch Loop 1 (SQL Generation)
loop1_regex = r'(for \(String key : searchParams\.keySet\(\)\) \{\s*if \(key\.startsWith\("op_"\) \|\| key\.endsWith\("_max"\)\) continue;)'
loop1_replacement = r'''\1
                
                if (key.equals("multi_keys")) continue;
                if (key.equals("multi_val")) {
                    String fieldsStr = searchParams.get("multi_keys");
                    if (fieldsStr == null || fieldsStr.isEmpty()) continue;
                    String[] fields = fieldsStr.split(",");
                    
                    StringBuilder multiCond = new StringBuilder(" AND ( ");
                    for (int i = 0; i < fields.length; i++) {
                        String f = fields[i].replaceAll("[^a-zA-Z0-9_]", "_");
                        if (i > 0) multiCond.append(" OR ");
                        if (isH2) {
                            multiCond.append(" (CAST(COALESCE(r.searchable_data, r.data) AS VARCHAR) LIKE '%\"").append(f).append("\":\"' || :searchValLike").append(paramIndex).append(" || '\"%' ")
                                     .append(" OR CAST(COALESCE(r.searchable_data, r.data) AS VARCHAR) LIKE '%\"").append(f).append("\":' || :searchValLike").append(paramIndex).append(" || ',%' ")
                                     .append(" OR CAST(COALESCE(r.searchable_data, r.data) AS VARCHAR) LIKE '%\"").append(f).append("\":' || :searchValLike").append(paramIndex).append(" || '}%' ) ");
                        } else {
                            multiCond.append(" ((NULLIF(CAST(COALESCE(r.searchable_data, r.data) AS jsonb)->>'").append(f).append("', '') ILIKE :searchValLike").append(paramIndex).append(") ")
                                     .append(" OR (NULLIF(CAST(COALESCE(r.searchable_data, r.data) AS jsonb)->>'").append(f.toLowerCase()).append("', '') ILIKE :searchValLike").append(paramIndex).append(") ")
                                     .append(" OR (NULLIF(CAST(COALESCE(r.searchable_data, r.data) AS jsonb)->'").append(f).append("'->>'ko', '') ILIKE :searchValLike").append(paramIndex).append(") ")
                                     .append(" OR (NULLIF(CAST(COALESCE(r.searchable_data, r.data) AS jsonb)->'").append(f).append("'->>'en', '') ILIKE :searchValLike").append(paramIndex).append(") ")
                                     .append(" OR (NULLIF(CAST(COALESCE(r.searchable_data, r.data) AS jsonb)->'").append(f.toLowerCase()).append("'->>'ko', '') ILIKE :searchValLike").append(paramIndex).append(") ")
                                     .append(" OR (NULLIF(CAST(COALESCE(r.searchable_data, r.data) AS jsonb)->'").append(f.toLowerCase()).append("'->>'en', '') ILIKE :searchValLike").append(paramIndex).append(")) ");
                        }
                    }
                    multiCond.append(" ) ");
                    sql.append(multiCond.toString());
                    countSql.append(multiCond.toString());
                    paramIndex++;
                    continue;
                }'''

content = re.sub(loop1_regex, loop1_replacement, content, count=1)

# Patch Loop 2 (Parameter Binding)
loop2_regex = r'(paramIndex = 0;\s*if \(searchParams != null\) \{\s*for \(String key : searchParams\.keySet\(\)\) \{\s*if \(key\.startsWith\("op_"\) \|\| key\.endsWith\("_max"\)\) continue;)'
loop2_replacement = r'''\1
                if (key.equals("multi_keys")) continue;
                if (key.equals("multi_val")) {
                    String val = searchParams.get(key);
                    String likeVal = "%" + val + "%";
                    query.setParameter("searchValLike" + paramIndex, likeVal);
                    countQuery.setParameter("searchValLike" + paramIndex, likeVal);
                    paramIndex++;
                    continue;
                }'''

content = re.sub(loop2_regex, loop2_replacement, content, count=1)

with open('src/main/java/com/classification/domain_system/repository/CustomRecordRepositoryImpl.java', 'w', encoding='utf-8') as f:
    f.write(content)
print("Patched successfully!")
