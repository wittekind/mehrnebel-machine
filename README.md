## Debugging via REST

### Set address of ArtNet Node

PUT to /artnet

```json
{
  "nodeAddress": "192.168.1.11"
}
```

### Set fogger DMX Address

PUT to /fogger/address

```json
{
  "dmxAddress": 0
}
```

### Start fog

POST to /fogger/fog

```json
{
  "fogIntensity": 255
}
```

Intensity can range from 0-255

### Stop fog

POST to /fogger/fog

```json
{
  "fogIntensity": 0
}
```