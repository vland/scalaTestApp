# WordLib schema

# --- !Ups

CREATE TABLE wordinfo
(
	name VARCHAR(255) PRIMARY KEY,
    value LONGTEXT
)

# --- !Downs

DROP TABLE wordinfo
