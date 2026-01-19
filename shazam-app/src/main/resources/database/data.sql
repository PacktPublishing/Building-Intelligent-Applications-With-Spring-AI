SELECT count(*) FROM songs;
SELECT * FROM songs;
TRUNCATE songs;

SELECT count(*) FROM vector_store;
SELECT * FROM vector_store;
TRUNCATE vector_store;

SELECT DISTINCT metadata::text FROM vector_store;
SELECT DISTINCT (metadata->>'songId')::uuid FROM vector_store;

SELECT DISTINCT artist, title FROM songs s, vector_store v
WHERE s.id = (v.metadata->>'songId')::uuid
ORDER BY s.artist, s.title ASC;
