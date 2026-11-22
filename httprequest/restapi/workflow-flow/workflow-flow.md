# Workflow Flow: hello-world-process starten und Token-Position verifizieren

Die Workflow-Flows sind in separate, eigenständig ausführbare Flows (Ordner) aufgeteilt.
Jeder Flow startet seinen eigenen Prozess, prüft Token-Variable und Token-Position über die
Engine-REST-API und schließt den User Task `say-hello` per Claim als `admin` und Complete ab.
Der Claim wartet per Poll, bis der Task aktiv ist.
Die beiden Validierungs-Flows erzeugen dagegen keine Instanz.

|      Flow       |             Quelldatei             |  Prozessstart   |                 Erwartung                  |
|-----------------|------------------------------------|-----------------|--------------------------------------------|
| `default`       | `default/default.http`             | `input=hallo`   | `200`, Default-Branch (`User03=Default`)   |
| `eder`          | `eder/eder.http`                   | `input=eder`    | `200`, Branch `eder` (`User03=Eder`)       |
| `pumukel`       | `pumukel/pumukel.http`             | `input=pumukel` | `200`, Branch `pumukel` (`User03=Pumukel`) |
| `fail`          | `fail/fail.http`                   | `input=fail`    | `200`, Fehlerpfad (`BpmnError`)            |
| `missing-input` | `missing-input/missing-input.http` | kein `input`    | `400` (Bean Validation)                    |
| `empty-input`   | `empty-input/empty-input.http`     | `input=""`      | `400` (`ValidateInputDelegate`)            |

Die Bruno-Sammlung liegt unter `httprequest/bruno/restapi/workflow-flow/<flow>/`.
Vor dem Start wird `processInstanceId` in jedem Flow zurückgesetzt.

## Ablauf (Services)

```mermaid
sequenceDiagram
    actor C as HTTP Client (IntelliJ/Bruno)
    participant APP as workflow-operaton-hello-world
    participant LDAP as OpenLDAP
    participant API as apifirst-server-jpa

    C->>APP: POST /restapi/workflow {input}
    APP->>LDAP: ValidateInputDelegate liest Users/Groups
    APP-->>C: 200 (ProcessInstance) oder 400 (Validierung)

    C->>APP: GET /engine-rest/process-instance/{id}/variables/tokenVariable
    APP-->>C: tokenVariable (status, input)

    C->>APP: GET /engine-rest/process-instance/{id}/activity-instances
    APP-->>C: aktuelle Activity (z. B. say-hello)

    C->>APP: GET /engine-rest/task?processInstanceId={id}
    APP-->>C: say-hello task
    C->>APP: POST /engine-rest/task/{taskId}/claim {"userId":"admin"}
    C->>APP: POST /engine-rest/task/{taskId}/complete {}

    Note over APP,API: Complete stößt SayHelloDelegate an; fail -> BpmnError
    APP->>API: SayHelloDelegate -> CustomerApi.listCustomers()
```

## Requests je Flow

|      Flow       | # |              Request               |               Erwartung                |
|-----------------|---|------------------------------------|----------------------------------------|
| `default`       | 1 | start process with input hallo     | `200`, Prozessinstanz angelegt         |
| `default`       | 2 | read the token variable            | `200`, Status gesetzt, Input `hallo`   |
| `default`       | 3 | read the token position (activity) | `200`, aktuelle Activity vorhanden     |
| `default`       | 4 | get the user task (say-hello)      | `200`, genau ein Task                  |
| `default`       | 5 | claim the user task as admin       | `204`                                  |
| `default`       | 6 | complete the user task             | `204`                                  |
| `eder`          | 1 | start process with input eder      | `200`, Prozessinstanz angelegt         |
| `eder`          | 2 | read the token variable            | `200`, Status gesetzt, Input `eder`    |
| `eder`          | 3 | read the token position (activity) | `200`, aktuelle Activity vorhanden     |
| `eder`          | 4 | get the user task (say-hello)      | `200`, genau ein Task                  |
| `eder`          | 5 | claim the user task as admin       | `204`                                  |
| `eder`          | 6 | complete the user task             | `204`                                  |
| `pumukel`       | 1 | start process with input pumukel   | `200`, Prozessinstanz angelegt         |
| `pumukel`       | 2 | read the token variable            | `200`, Status gesetzt, Input `pumukel` |
| `pumukel`       | 3 | read the token position (activity) | `200`, aktuelle Activity vorhanden     |
| `pumukel`       | 4 | get the user task (say-hello)      | `200`, genau ein Task                  |
| `pumukel`       | 5 | claim the user task as admin       | `204`                                  |
| `pumukel`       | 6 | complete the user task             | `204`                                  |
| `fail`          | 1 | start process with input fail      | `200`, Prozessinstanz angelegt         |
| `fail`          | 2 | read the token variable            | `200`, Status gesetzt, Input `fail`    |
| `fail`          | 3 | read the token position (activity) | `200`, aktuelle Activity vorhanden     |
| `fail`          | 4 | get the user task (say-hello)      | `200`, genau ein Task                  |
| `fail`          | 5 | claim the user task as admin       | `204`                                  |
| `fail`          | 6 | complete the user task             | `204` (BpmnError)                      |
| `missing-input` | 1 | start process without input field  | `400` (Bean Validation)                |
| `empty-input`   | 1 | start process with empty input     | `400` (`ValidateInputDelegate`)        |

## Token-Verifikation

Die `tokenVariable` wird im `ValidateInputDelegate` erzeugt und durchläuft folgende Status:

|        Status        |                Bedeutung                 |
|----------------------|------------------------------------------|
| `STARTED`            | Token nach der Input-Validierung erzeugt |
| `RUNNING`            | Token im External Task (`sayHelloTopic`) |
| `COMPLETED`          | User Task `say-hello` abgeschlossen      |
| `FINISHED`           | Prozess erfolgreich beendet              |
| `BUSINESS_EXCEPTION` | Fehlerpfad (`fail`)                      |

Die Activity-Abfrage liefert die aktuelle Position, z. B. `Service_for_Script`,
`External_Task`, `say-hello` oder `Activity_say_hello-via_delegate`. Im Regelfall steht der
Token nach dem Start am User Task `say-hello` (Status `RUNNING`).

## Asserts

- Start-Requests in `default`, `eder`, `pumukel` und `fail`: `200`,
  `processDefinitionId` beginnt mit `hello-world-process`, `processInstanceId` gesetzt und
  als `{{processInstanceId}}` gespeichert.
- Token-Requests: `200`, gueltiger Status, erwarteter Input.
- Activity-Requests: `200`, aktuelle Activity/Transition vorhanden, gleiche Prozessinstanz.
- Task-Query: `200` (Task ggf. noch nicht aktiv); Claim pollt, Claim/Complete: `204`.
- `missing-input` und `empty-input`: `400`.

## Hinweise

- Der Prozessstart ruft `ValidateInputDelegate` synchron auf; dieser benoetigt LDAP
  (`docker compose up -d ldap` bzw. der Kubernetes-Stack).
- Nach dem Start laufen Script-, External-Task- und User-Task-Schritte asynchron. Die
  Token-/Activity-Pruefung ist daher tolerant formuliert und akzeptiert die jeweilige
  Zwischenposition. In CI sorgt `bru run ... --delay 6000` dafuer, dass der Token in der
  Regel bereits am User Task `say-hello` steht.
- Der `fail`-Pfad wird erst im `SayHelloDelegate` nach Abschluss des User Tasks ausgeloest;
  der Controller antwortet trotzdem mit `200`, weil der Prozess zu diesem Zeitpunkt bereits laeuft.
- Der Default-Pfad setzt `User03` auf `Default`, die Branches auf `Eder` bzw. `Pumukel`
  (`Set_User03_*`-ScriptTasks im BPMN).
- Die vier Start-Flows claimen `say-hello` als `admin` und completen den Task; danach läuft
  der Prozess weiter. Beim `fail`-Flow wirft erst der `SayHelloDelegate` nach dem Complete den
  `BpmnError`.
- Die Claim-Requests pollen in Bruno die Task-Query (`bru.runRequest` + `bru.sleep`), bis der
  `say-hello`-Task aktiv ist. Der 04-Request ist eine tolerante Vorab-Abfrage; im
  IntelliJ-HTTP-Client gibt es keinen Sleep, dort ggf. die Query erneut ausführen.

