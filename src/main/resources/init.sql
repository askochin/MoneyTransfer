-- noinspection SqlNoDataSourceInspectionForFile

create table Account (
  AccountId varchar(64) primary key,
  Balance decimal(20,2)
);

create table Transfer (
  TransferId bigint identity primary key,
  SourceAccountId varchar(64),
  DestAccountId varchar(64),
  Sum decimal(20,2)
);

insert into Account values('a1', 100);
insert into Account values('a2', 200);
insert into Account values('a3', 300);
