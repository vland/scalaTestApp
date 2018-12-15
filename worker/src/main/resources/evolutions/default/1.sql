# WordLib schema

# --- !Ups

create table wordinfo
(
	name varchar(255) primary key,
    value text
)

# --- !Downs

drop table wordinfo
