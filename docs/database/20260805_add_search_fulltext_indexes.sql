-- Search optimization prerequisites:
-- MySQL 8.4 / InnoDB / ngram_token_size = 2
--
-- Apply while application writes are stopped.
-- Do not execute again when indexes already exist.

ALTER TABLE posts
    ADD FULLTEXT INDEX ft_post_search (title, content)
    WITH PARSER ngram,
    ALGORITHM=INPLACE,
    LOCK=SHARED;

ALTER TABLE users
    ADD FULLTEXT INDEX ft_user_nickname (nickname)
    WITH PARSER ngram,
    ALGORITHM=INPLACE,
    LOCK=SHARED;

-- Verification
SHOW INDEX FROM posts
WHERE Key_name = 'ft_post_search';

SHOW INDEX FROM users
WHERE Key_name = 'ft_user_nickname';

-- Manual rollback:
-- ALTER TABLE posts DROP INDEX ft_post_search;
-- ALTER TABLE users DROP INDEX ft_user_nickname;
