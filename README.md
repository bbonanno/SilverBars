Silver Bars
===========

SilverBars is a REST API designed to be consumed either by a GUI or another back-end service


## API

| Path                          |  Methods  | Description                              |
|-------------------------------|-----------|------------------------------------------|
| `/silverbars/orders`           | `POST`    | Registers a new order      |
| `/silverbars/orders/:id`       | `DELETE`  | Cancels an existing order   |
| `/silverbars/orders/summary`   | `GET`     | Gets a summary of all the orders currently alive in the system     |


#### POST /silverbars/orders
Creates an order and returns it's location

##### Response with

| Status | Description                          |
|--------|--------------------------------------|
| 201    | Created                              |

##### Example of usage

**Request URL**

POST /silverbars/orders

**Request body**

```json
{ 
  "userId":"user1",
  "quantity":3.5,
  "price":306,
  "orderType":"Sell"
}
```

**Response body**

None

Headers: Location -> /silverbars/orders/96988d89-54a4-4ab7-af3f-44426081b942

#### GET /silverbars/orders/summary
Gets the summary of orders currently alive on the system

##### Response with

| Status | Description        |
|--------|--------------------|
| 200   | report     |

##### Example of usage

**Request URL**

GET /silverbars/orders/summary

**Response body**

```json
{
  "items":[
    {
      "quantity":3.5,
      "price":306,
      "orderType":"Sell"
    }
  ]
}
```

#### DELETE /silverbars/orders/:id
Deletes the the order for the given `id`.

##### Response with

| Status | Description                          |
|--------|--------------------------------------|
| 204    | Deleted                              |

##### Example of usage

**Request URL**

DELETE /silverbars/orders/96988d89-54a4-4ab7-af3f-44426081b942

**Request body**

None

**Response body**

None
