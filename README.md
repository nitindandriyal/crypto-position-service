# Position Service

Points to note
- Simple single threaded implementation
- Main class: crypto.com.position.Main

StockTickSubscriber
------------------------------------
- Subscribe to the market data events over Aeron
- Reads the message[```ticker,price```] in that order, keeping it simple for sake of exercise. In a real scenario, there may be many there may be many message types
  and need a well-defined protocol for events.
- ```StockTickSubscriber``` maintains a list of subscribers(no duplicate check done) which implement ```TickEventHandler```. on every price update it broadcasts the event to all ```TickEventHandlers``` 

PositionManager
--------------------------------------
- On initialization[```init()```], loads the initial snapshot of positions and also publishes this snapshot to the ```PositionEventHandler.snapshot()```
  Snapshot consists of ticker name and quantities
- Also, Implements ```TickEventHandler```, which means it receives the price updated from ```StockTickSubscriber```. On every price update it calls the ```PositionEventHandler.update()``` 




