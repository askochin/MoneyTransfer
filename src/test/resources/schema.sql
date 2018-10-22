
create table Account (
  AccountId varchar(64) primary key,
  Balance decimal(20,2)
);

create table Transfer (
  TransferId bigint identity primary key,
  SourceAccountId varchar(64),
  DestAccountId varchar(64),
  Amount decimal(20,2)
);
