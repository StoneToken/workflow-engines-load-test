## <a name="d886f397-e962-46fc-bed2-8676c962b770"></a> d886f397-e962-46fc-bed2-8676c962b770 (TestCase_RestTask7_Sequential)
![alt text](../png/TestCase_RestTask7_Sequential.PNG "TestCase_RestTask7_Sequential")

### 01 Start Event
Принимает Message  
**Event**

| Event Type | Event Details
| :---       | :---            
| Message    | TestCase_RestTask7_Sequential

Формат передаваемого сообщения (Body)
```json
{
    "startMessageName": "TestCase_RestTask7_Sequential",
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

### 02 Rest Task1, 03 Rest Task2, 04 Rest Task3, 05 Rest Task4, 06 Rest Task5, 07 Rest Task6, 08 Rest Task7

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
| resultRest[1,2,3,4,5,6,7] | @RESULT.myObject2.myObj

### 09 End Event