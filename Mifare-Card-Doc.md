## Mifare Card Operation

Blow introduce how to transmit datas on the different mifare cards and pos.There are three typr mifare card - Mifare Classic, Mifare Ultralight, Mifare Desfire.

**1.Mifare Classic**

![](./mifare_card.png)

1).poll on Card

```java
pos.activateMifareCard(int timeout)
//callback
onActivateMifareCardResult(Hashtable<String, String> arg0)
```

2).Verify Key A/B

```java
pos.authenticateMifareCard(MifareCardType cardType,String keyType,String block,String keyValue,int timeout)
//callback
onAuthenticateMifareCardResult(boolean arg0)
```

3).Operate Card

- Add

```java
pos.increaseValue(String block,String data,int timeout)
//callback
onIncreaseValueResult(boolean result)
```

- Reduce

```java
pos.decreaseValue(String block,String data,int timeout)
//callback
onDecreaseValueResult(boolean result)
```

- Read Value

```java
pos.readMifareValue(String block,int timeout)
//callback
onReadMifareValueResult(int flag)
```

- Read Block

```java
pos.readMifareBlock(MifareCardType cardType,String block,int timeout)
//callback
onReadMifareBlockResult(String flag)
```

- Write Value

```java
pos.writeMifareValue(String block,int value,int timeout)
//callback
onWriteMifareValueResult(boolean flag)
```

- Write Block

```java
pos.writeMifareBlock(MifareCardType cardType,String block,String data,int timeout)
//callback
onWriteMifareBlockResult(boolean arg0)
```

- Transfer Block

```java
pos.transferBlock(String block)
//callback
onTransferValueResult(boolean flag)
```

4).Finish

```java
pos.deactivateMifareCard(int timeout)
//callback
onDeactivateMifareCardResult(boolean arg0)
```



**2.Mifare Ultralight**

  The Ultralight card most operate is same with the classic card, except some part is different.

1. It don't need to use key A/B to verify, just verify the data.
2. It don't have the Add/Reduce/Restore operation,but can read and write data.
3. It have a special method to read data.

```java
pos.fastReadMifareCardData(String startBlock,String endBlock,int timeout)
//callback
getMifareFastReadData(Hashtable<String, String> flag)
```

4. For write and read mifare block, it has different callback:

- Read Block

```java
pos.readMifareBlock(MifareCardType cardType,String block,int timeout)
//callback
getMifareReadData(Hashtable<String, String> flag)
```

- Write Block

```java
pos.readMifareBlock(MifareCardType cardType,String block,int timeout)
//callback
writeMifareULData(String flag)
```



**3.Mifare Desfire**

1. Power on card

```java
pos.powerOnNFC(int isEncrypt, int timeout)
//callback
onReturnPowerOnNFCResult(boolean result, QPOSService.CardsType cardType, String atr, int atrLen)
```

2. Send apdu data

```java
pos.sendApduByNFC(String apduString, int timeout)
//callback
onReturnNFCApduResult(boolean result, String apdu, int apduLen) 
```

3. Power off card

```java
pos.powerOffNFC(int timeout)
//callback
onReturnPowerOffNFCResult(boolean result)
```



