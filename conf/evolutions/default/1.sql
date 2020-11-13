# -- !Ups

CREATE TABLE  "TrafficLights" (
                         id          bigint(20) NOT NULL UNIQUE,
                         color      varchar(255) NOT NULL
);

# -- !Downs

DROP TABLE "TrafficLights"