create table customers (
    id               bigint auto_increment primary key,
    full_name        varchar(100) not null,
    father_name      varchar(100) not null,
    date_of_birth    date         not null,
    gender           varchar(10)  not null,
    email            varchar(254) not null,
    marital_status   varchar(20)  not null,
    address          varchar(300) not null,
    city             varchar(80)  not null,
    pin_code         varchar(6)   not null,
    occupation       varchar(30)  not null,
    income_range     varchar(30)  not null,
    pan_encrypted    varchar(200) not null,
    aadhaar_last4    varchar(4)   not null,
    created_at       timestamp(6) not null
);

create table accounts (
    id               bigint auto_increment primary key,
    customer_id      bigint        not null,
    account_type     varchar(20)   not null,
    card_number      varchar(16)   not null,
    pin_hash         varchar(100)  not null,
    balance          decimal(19,2) not null default 0.00,
    failed_attempts  int           not null default 0,
    locked_until     timestamp(6) null,
    created_at       timestamp(6)  not null,
    constraint accounts_card_number_key unique (card_number),
    constraint accounts_balance_non_negative check (balance >= 0),
    constraint accounts_customer_fk foreign key (customer_id) references customers (id)
);

create index accounts_customer_idx on accounts (customer_id);

create table ledger_entries (
    id               bigint auto_increment primary key,
    account_id       bigint        not null,
    entry_type       varchar(20)   not null,
    amount           decimal(19,2) not null,
    balance_after    decimal(19,2) not null,
    counterpart_card varchar(16),
    request_id       binary(16)    not null,
    created_at       timestamp(6)  not null,
    constraint ledger_entries_amount_positive check (amount > 0),
    constraint ledger_entries_request_key unique (account_id, request_id),
    constraint ledger_entries_account_fk foreign key (account_id) references accounts (id)
);

create index ledger_entries_account_time_idx
    on ledger_entries (account_id, created_at desc, id desc);
