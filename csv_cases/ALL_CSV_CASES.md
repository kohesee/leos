# Warehouse OS CSV Case Pack

Use any one case by copying its CSV block into [multi_user_tasks.csv](../multi_user_tasks.csv), then run the simulator.

## Common CSV Header

```csv
userId,taskId,priority,aisleId,memorySize,processTime
```

---

## Case 01 - Normal (Distinct Aisles, No Contention)

Expected: smooth run, no waiting on semaphores.

```csv
userId,taskId,priority,aisleId,memorySize,processTime
1,101,1,1,40,10
2,102,1,3,45,12
3,103,2,5,35,9
```

## Case 02 - Single Aisle Contention

Expected: semaphore queueing/blocking on aisle 2.

```csv
userId,taskId,priority,aisleId,memorySize,processTime
1,201,1,2,40,12
2,202,1,2,45,10
3,203,2,2,35,9
```

## Case 03 - Two-Aisle Hotspot Conflict

Expected: repeated contention between aisle 1 and 2 users.

```csv
userId,taskId,priority,aisleId,memorySize,processTime
1,301,1,1,40,10
2,302,1,2,45,11
3,303,2,1,35,12
4,304,2,2,30,8
```

## Case 04 - High Contention Deadlock-Risk Style

Expected: unsafe checks/deferrals under heavy overlap.

```csv
userId,taskId,priority,aisleId,memorySize,processTime
1,401,1,1,45,13
2,402,1,1,40,12
3,403,1,2,35,11
4,404,1,2,30,10
```

## Case 05 - Priority Dominance

Expected: priority 1 tasks selected earlier than 3.

```csv
userId,taskId,priority,aisleId,memorySize,processTime
1,501,3,4,40,14
2,502,1,1,45,8
3,503,1,3,35,9
4,504,3,6,30,13
```

## Case 06 - Round-Robin Focus (All Priority 3)

Expected: RR behavior among equal low-priority tasks.

```csv
userId,taskId,priority,aisleId,memorySize,processTime
1,601,3,1,40,20
2,602,3,3,45,18
3,603,3,5,35,17
4,604,3,7,30,19
```

## Case 07 - Memory Near Capacity (Successful)

Expected: high memory use but all tasks fit.

```csv
userId,taskId,priority,aisleId,memorySize,processTime
1,701,1,1,130,10
2,702,1,3,120,11
3,703,2,5,125,12
4,704,2,7,120,9
```

## Case 08 - Memory Over Capacity (Allocation Failure Case)

Expected: at least one task cannot allocate memory.

```csv
userId,taskId,priority,aisleId,memorySize,processTime
1,801,1,1,180,10
2,802,1,3,170,9
3,803,2,5,160,11
4,804,2,7,120,8
```

## Case 09 - Fragmentation-Friendly Sizes

Expected: varied partition sizes, useful for memory map observation.

```csv
userId,taskId,priority,aisleId,memorySize,processTime
1,901,1,1,55,9
2,902,1,3,140,14
3,903,2,5,65,8
4,904,2,7,120,13
```

## Case 10 - Multi-User Fair Mix

Expected: balanced users, moderate contention, good demo baseline.

```csv
userId,taskId,priority,aisleId,memorySize,processTime
1,1001,1,1,45,10
2,1002,2,2,40,12
3,1003,1,3,35,11
4,1004,2,4,30,9
```

## Case 11 - Convoy Effect Style (One Long Task)

Expected: one long task can increase waits around shared resources.

```csv
userId,taskId,priority,aisleId,memorySize,processTime
1,1101,1,2,50,35
2,1102,1,2,40,8
3,1103,2,4,35,9
```

## Case 12 - Fast Burst Tasks

Expected: low turnaround, quick completion cluster.

```csv
userId,taskId,priority,aisleId,memorySize,processTime
1,1201,1,1,30,4
2,1202,1,3,30,5
3,1203,2,5,25,4
4,1204,2,7,20,5
```

## Case 13 - I/O + Concurrency Mixed Stress

Expected: overlap of aisle contention and I/O path logging.

```csv
userId,taskId,priority,aisleId,memorySize,processTime
1,1301,1,6,60,16
2,1302,1,6,55,14
3,1303,2,8,50,15
4,1304,3,8,45,13
```

## Case 14 - Two Users, Same Priority, Cross Contention

Expected: clean comparison of user-level fairness.

```csv
userId,taskId,priority,aisleId,memorySize,processTime
1,1401,1,1,40,11
1,1402,1,2,35,10
2,1403,1,1,40,12
2,1404,1,2,35,9
```

## Case 15 - Minimal Sanity Case (3 Tasks)

Expected: simplest full-flow demonstration.

```csv
userId,taskId,priority,aisleId,memorySize,processTime
1,1501,1,1,40,10
2,1502,2,3,40,11
3,1503,3,5,40,12
```

---

## Quick Run

```powershell
java -cp bin CompleteMultiUserOrchestrator multi_user_tasks.csv
```
