## <a name="b3a04993-3d9d-4333-980a-96025557c18a"></a> b3a04993-3d9d-4333-980a-96025557c18a (TestCase_RestTask1)
Модель с шагом Rest Task  
![alt text](../png/TestCase_RestTask1.PNG "TestCase_RestTask1")

### 01 Start Event
Принимает Message  
**Event**

| Event Type | Event Details
| :---       | :---            
| Message    | TestCase_RestTask1

Формат передаваемого сообщения (Body)
```json
{
    "startMessageName": "TestCase_RestTask1",
    "payload": {
      "baseUrl" : "stand.urlModule.Service",
      "serviceUrl" : "stand.urlTestModule.Service",
      "serviceMmtUrl" : "stand.urlTestMmtModule.Service",
      "objectId": "test",
      "variableCount": 5,
      "dataMapCount": 1,
      "subProcess1TimerSec": 5,
      "incomingValue": 100,
      "exitTimerDuration": "PT10S"
    }
}
```

### 02 Rest Task 1

| Параметр     | Значение
| :---         | :---   
| URL          | ${serviceUrl}/rest/object/${execution.getProcessInstanceId()}
| Method Name  | GET
| Timeout      | PT10S  
| Retry Policy | 

**HTTP Header List**  

| Name | Value
| :--- |  :---
| Content-Type | application/json; charset=UTF-8;

**Out Parameter List**  

| Variable Name | Expression
| :--- |  :---
| resultRest1 | @RESULT.myObject2.myObj

### 03 End Event