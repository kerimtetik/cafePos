-- MASA
create table cafe_table (
  id identity primary key,
  number int not null unique,
  capacity int not null default 2,
  status varchar(20) not null default 'EMPTY' -- EMPTY, OCCUPIED, BILL
);

-- SIPARIS (header)
create table order_header (
  id identity primary key,
  table_id bigint not null,
  status varchar(20) not null default 'OPEN', -- OPEN, IN_KITCHEN, SERVING, CLOSED, CANCELED
  created_at timestamp not null default current_timestamp(),
  closed_at timestamp,
  channel varchar(20) not null default 'DINEIN',
  constraint fk_order_table foreign key (table_id) references cafe_table(id)
);

-- SIPARIS KALEMLERI
create table order_item (
  id identity primary key,
  order_id bigint not null,
  product_id bigint not null,
  qty int not null default 1,
  unit_price decimal(10,2) not null,
  note varchar(200),
  status varchar(20) not null default 'NEW', -- NEW, PREP, READY, SERVED, CANCELED
  constraint fk_item_order foreign key (order_id) references order_header(id),
  constraint fk_item_product foreign key (product_id) references product(id)
);

-- örnek masalar
insert into cafe_table(number, capacity) values (1,2),(2,2),(3,4),(4,4);
