delete from orders;
delete from orderlines;
delete from customers;
delete from products;

insert into customers (id, name) values (123456, 'Devin Snow');
insert into customers (id, name) values (789101, 'Homer Simpson');
insert into customers (id, name) values (999999, 'John Doe');

insert into products  (id, name) values (11, 'Cheese cake');
insert into products  (id, name) values (22, 'Chocolate 300gr');
insert into products  (id, name) values (33, 'Beer');
insert into products  (id, name) values (44, 'Smooks in Action');
insert into products  (id, name) values (55, 'Ultimate guide to Smooks');