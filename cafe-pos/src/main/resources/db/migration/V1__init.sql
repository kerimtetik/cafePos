create table category (
  id identity primary key,
  name varchar(100) not null,
  sort int not null default 0
);

create table product (
  id identity primary key,
  name varchar(150) not null,
  price decimal(10,2) not null,
  category_id bigint not null,
  is_active boolean not null default true,
  constraint fk_product_category foreign key (category_id) references category(id)
);

-- örnek veri
insert into category(name, sort) values ('Kahve', 1), ('Tatlı', 2);
insert into product(name, price, category_id) values
('Espresso', 55.00, 1),
('Americano', 60.00, 1),
('Cheesecake', 95.00, 2);

