# Backend API Requirements for MiniBoss Persistence

## Required Endpoint

### Save MiniBoss Defeated State
```
POST /api/players/{username}/miniboss
Content-Type: application/json

Request Body:
{
  "miniBossDefeated": true
}

Response: 200 OK
```

### Load Player Data (Updated)
```
POST /api/players/login?username={username}

Response Body:
{
  "username": "player1",
  "inventory": {
    "WOOD": 10,
    "STONE": 5
  },
  "miniBossDefeated": false
}
```

## Database Schema Update

Add column to player table:
```sql
ALTER TABLE players ADD COLUMN mini_boss_defeated BOOLEAN DEFAULT FALSE;
```

## Implementation Notes

1. When player logs in, return `miniBossDefeated` field
2. When client calls `/miniboss` endpoint, update the database
3. Default value should be `false` for new players
4. State persists across sessions
