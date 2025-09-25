# JBudget - Istruzioni di avvio

Per avviare l'applicazione:

1. Apri un terminale o un prompt dei comandi nella cartella root del progetto.
2. Esegui il comando:

   - **Su Windows:**
     ```bash
     .\gradlew run
     ```
   - **Su macOS/Linux:**
     ```bash
     ./gradlew run
     ```

---

## Database

L'applicazione utilizza un database H2 su file per memorizzare tutti i dati. Ho fornito un database con dei dati di partenza già disponibili per il testing.

Il file del database verrà creato automaticamente al primo avvio e si troverà nel seguente percorso relativo alla cartella del progetto:
```bash
./data/database.mv.db
```
> ⚠️ È importante non cancellare questa cartella per non perdere i dati salvati.
