-- Message

-- Message-Prop-Type
insert ignore into message (message_id, created_date, is_enable) values ('PROP_TYPE_STRN', now(), 'Y');
insert ignore into message_lang (message_id, lang, message, created_date) values ('PROP_TYPE_STRN', 'ko', '문자열', now());
insert ignore into message_lang (message_id, lang, message, created_date) values ('PROP_TYPE_STRN', 'en', 'String', now());
insert ignore into message (message_id, created_date, is_enable) values ('PROP_TYPE_COMBO', now(), 'Y');
insert ignore into message_lang (message_id, lang, message, created_date) values ('PROP_TYPE_COMBO', 'ko', '콤보박스', now());
insert ignore into message_lang (message_id, lang, message, created_date) values ('PROP_TYPE_COMBO', 'en', 'Select box', now());
insert ignore into message (message_id, created_date, is_enable) values ('PROP_TYPE_DATE', now(), 'Y');
insert ignore into message_lang (message_id, lang, message, created_date) values ('PROP_TYPE_DATE', 'ko', '날짜', now());
insert ignore into message_lang (message_id, lang, message, created_date) values ('PROP_TYPE_DATE', 'en', 'Date', now());
insert ignore into message (message_id, created_date, is_enable) values ('PROP_TYPE_EDITOR', now(), 'Y');
insert ignore into message_lang (message_id, lang, message, created_date) values ('PROP_TYPE_EDITOR', 'ko', 'HTML 에디터', now());
insert ignore into message_lang (message_id, lang, message, created_date) values ('PROP_TYPE_EDITOR', 'en', 'HTML editor', now());
insert ignore into message (message_id, created_date, is_enable) values ('PROP_TYPE_FILE', now(), 'Y');
insert ignore into message_lang (message_id, lang, message, created_date) values ('PROP_TYPE_FILE', 'ko', '파일', now());
insert ignore into message_lang (message_id, lang, message, created_date) values ('PROP_TYPE_FILE', 'en', 'File', now());
insert ignore into message (message_id, created_date, is_enable) values ('PROP_TYPE_NVAL', now(), 'Y');
insert ignore into message_lang (message_id, lang, message, created_date) values ('PROP_TYPE_NVAL', 'ko', '숫자', now());
insert ignore into message_lang (message_id, lang, message, created_date) values ('PROP_TYPE_NVAL', 'en', 'Number', now());
insert ignore into message (message_id, created_date, is_enable) values ('PROP_TYPE_TEXTAREA', now(), 'Y');
insert ignore into message_lang (message_id, lang, message, created_date) values ('PROP_TYPE_TEXTAREA', 'ko', '문자열(행)', now());
insert ignore into message_lang (message_id, lang, message, created_date) values ('PROP_TYPE_TEXTAREA', 'en', 'Textarea', now());
insert ignore into message (message_id, created_date, is_enable) values ('PROP_TYPE_CODE', now(), 'N');
insert ignore into message_lang (message_id, lang, message, created_date) values ('PROP_TYPE_CODE', 'ko', '코드', now());
insert ignore into message_lang (message_id, lang, message, created_date) values ('PROP_TYPE_CODE', 'en', 'Code', now());

-- Message-Section
insert ignore into message (message_id, created_date, is_enable) values ('GROUP_STANDARD', now(), 'Y');
insert ignore into message_lang (message_id, lang, message, created_date) values ('GROUP_STANDARD', 'ko', '표준 속성', now());
insert ignore into message_lang (message_id, lang, message, created_date) values ('GROUP_STANDARD', 'en', 'Standard Props', now());

-- Message-Group
insert ignore into message (message_id, created_date, is_enable) values ('SECTION_BASIC', now(), 'Y');
insert ignore into message_lang (message_id, lang, message, created_date) values ('SECTION_BASIC', 'ko', '기본정보', now());
insert ignore into message_lang (message_id, lang, message, created_date) values ('SECTION_BASIC', 'en', 'Default Info.', now());

-- Message-Area
insert ignore into message (message_id, created_date, is_enable) values ('AREA_DEFAULT', now(), 'Y');
insert ignore into message_lang (message_id, lang, message, created_date) values ('AREA_DEFAULT', 'ko', '*', now());
insert ignore into message_lang (message_id, lang, message, created_date) values ('AREA_DEFAULT', 'en', '*', now());

-- Prop_Type
insert ignore into prop_type (type, message_id, description, created_date) values ('STRN', 'PROP_TYPE_STRN', '"Example Text"', now());
insert ignore into prop_type (type, message_id, description, created_date) values ('CODE', 'PROP_TYPE_CODE', 'CODE1', now());
insert ignore into prop_type (type, message_id, description, created_date) values ('COMBO', 'PROP_TYPE_COMBO', '[SELECT1, SELECT2...]', now());
insert ignore into prop_type (type, message_id, description, created_date) values ('DATE', 'PROP_TYPE_DATE', '2020-02-01', now());
insert ignore into prop_type (type, message_id, description, created_date) values ('EDITOR', 'PROP_TYPE_EDITOR', '<p>HTML Editor</p>', now());
insert ignore into prop_type (type, message_id, description, created_date) values ('FILE', 'PROP_TYPE_FILE', 'Document File', now());
insert ignore into prop_type (type, message_id, description, created_date) values ('NVAL', 'PROP_TYPE_NVAL', '1234', now());
insert ignore into prop_type (type, message_id, description, created_date) values ('TEXTAREA', 'PROP_TYPE_TEXTAREA', 'String Line...', now());


-- Section
insert ignore into msection (section_id, message_id, disp_seq, is_enable, created_date) values ('BASIC', 'SECTION_BASIC', 1, 'Y', now());

-- Group
insert ignore into mgroup (section_id, group_id, message_id, disp_seq, is_enable, created_date) values ('BASIC', 'STANDARD', 'GROUP_STANDARD', 1, 'Y', now());

-- Area
insert ignore into area (area_id, message_id, status, created_date) values ('*', 'AREA_DEFAULT', 'Y', now());


-- User
insert ignore into user (user_id, created_date, user_name) values ('TEST_USER1', now(), '테스트 유저1');
insert ignore into user (user_id, created_date, user_name) values ('TEST_USER2', now(), '테스트 유저2');
insert ignore into user (user_id, created_date, user_name) values ('TEST_USER3', now(), '테스트 유저3');
insert ignore into user (user_id, created_date, user_name) values ('TEST_USER4', now(), '테스트 유저4');
insert ignore into user (user_id, created_date, user_name) values ('test_id1', now(), 'Profavor');
insert ignore into user (user_id, created_date, user_name) values ('test_id2', now(), 'Staricex');