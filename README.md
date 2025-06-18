# TestAIA
AIA

# Azure Function App Intern Task - HTTP Trigger (Java)

## Task 1: Modify the Function to Return JSON using Query Parameters

### Objective:
Update the function to accept a `name` via **query parameter** and return a **JSON response**.

### Input:
GET request to:
```
https://testaiaintern.azurewebsites.net/api/HttpExample?
```

### Expected JSON Response:
json
{
  "message": "Hello, John"
}


---

## Task 2: Accept POST Request with JSON and Respond with JSON

### Objective:
Modify the function to:
- Accept a `POST` request with the following JSON body:
```json
{
  "firstName": "John",
  "lastName": "Dawson"
}
```

- Return a JSON response:
```json
{
  "message": "Hello, John Dawson"
}
```

### Requirements:
- Use ObjectMapper to parse JSON
- Return response in JSON format
- Respond with an appropriate message when required fields are missing

---

## Bonus (Optional):
- Validate fields like `firstName` and `lastName`
- Return HTTP 400 if fields are missing
- Add unit test cases in `FunctionTest.java`
