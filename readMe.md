# Dspread POS Android Demo Application

## Overview
A comprehensive Point of Sale (POS) Android application that demonstrates payment processing capabilities using Dspread's POS SDK, offering secure and efficient transaction handling for retail environments.

## Features
- **Payment Processing**: Supports various card payment methods including swipe, tap, and insert transactions
- **Receipt Generation And Printing**: Generate and print transaction receipts
- **Transaction History**: View and manage completed transactions
- **Device Configuration**: Connect to and configure POS devices via Bluetooth, USB, or UART

## Preview
![Dspread demo operation](_gif/Dspread demo operation.gif)

## Quick Start

### Environment Requirements
- Gradle 7.5+
- Target Android SDK 34 (API Level 34)
- Minimum supported Android SDK 24 (Android 7.0 Nougat)
- Java 11 compatible development environment

### Installation and Setup
1. Clone the repository:
```bash
git clone https://github.com/DspreadOrg/pos_demo.git
cd pos_demo/pos_android_studio_demo
```

2. Open the project in Android Studio

3. Sync the project with Gradle files

4. Build and run the application on a compatible Android device 

   ​

   Tip:  If you want to integrate SDK, please follow as below:

   ​

## Integration

### POSManager
The `POSManager` class is the core component for interacting with POS devices. And we implement it in `PaymentActivity`. Here's a brief overview of its key features and functionality:

```java
// Initialize POSManager
POSManager.init(context);

// Start payment transaction in background thread
// Handles device connection and transaction initialization
private void startTransaction() {
  new Thread(() -> {
    if(!POSManager.getInstance().isDeviceReady()){
      POSManager.getInstance().connect(deviceAddress,new ConnectionServiceCallback() {
        @Override
        public void onRequestNoQposDetected() {
        }

        @Override
        public void onRequestQposConnected() {
          ToastUtils.showLong("Device connected");
        }

        @Override
        public void onRequestQposDisconnected() {
          ToastUtils.showLong("Device disconnected");
          finish();
        }
      });
    }
    POSManager.getInstance().startTransaction(amount, paymentServiceCallback);
  }).start();
}

// Inner class to handle payment callbacks
// Implements all payment related events and UI updates
private class PaymentCallback implements PaymentServiceCallback {

  @Override
  public void onRequestTime() {
    // Send current time.
    POSManager.getInstance().sendTime(terminalTime);
  }

  @Override
  public void onRequestSelectEmvApp(ArrayList<String> appList) {
    // Select one application of the application list returned from EMV kernel, then set the application ID to EMV kernel
  	POSManager.getInstance().selectEmvApp(position);
    // Cancel the process about setting the application
    POSManager.getInstance().cancelSelectEmvApp();
  }

  @Override
  public void onQposRequestPinResult(List<String> dataList, int offlineTime) {
  	// Send the pin keyboard datas to Smart Device for pin input.
  	POSManager.getInstance().pinMapSync(value, 20);
  }

  @Override
  public void onRequestSetPin() {
    if("D70".equals(Build.MODEL)||"D80K".equals(Build.MODEL)){
    	// Enrtry PIN on physical keyboard
      	..
    } else {//CR100 devices
    	// Set cancel input pin to CR100
    	POSManager.getInstance().cancelPin();
		// Bypass pin input to CR100
    	POSManager.getInstance().bypassPin();
		// Input the cipher pinblock on the client app side to CR100
    	POSManager.getInstance().sendCvmPin(pinBlock, true);
    }
  }

  @Override
  public void onRequestDisplay(QPOSService.Display displayMsg) {
	// Return relevant prompt information.
    	..
  }

  @Override
  public void onDoTradeResult(QPOSService.DoTradeResult result, Hashtable<String, String> decodeData) {
    if(result == QPOSService.DoTradeResult.ICC){
      // Send the command as executing the EMV transaction flow to POS
      POSManager.getInstance().doEmvAPP();
    }else{
      // Return the transaction result of NFC and Swipe mode
      	..
    }
  }

  @Override
  public void onTransactionResult(boolean isCompleteTxns, PaymentResult result) {
  	// Return the transaction result
    	..
  }

  @Override
  public void onRequestOnlineProcess(final String tlv) {
	// Send online message tlv data to backend server
  	viewModel.requestOnlineAuth(true, paymentModel);
    // If online request successful, call
    POSManager.getInstance().sendOnlineProcessResult("8A02" + onlineRspCode);
  }

  @Override
  public void onReturnGetPinInputResult(int num, QPOSService.PinError error, int minLen, int maxLen) {
	// This is used for smart POS to return the number of pin inputs
    	..
  }
}
```

### Documentation
-  [**Qpos doc **](https://dspreadorg.github.io/qpos/#/introduction) :Please check the link to know more about the process, including connection, transaction, data decryption and more.
-  **[API reference](https://github.com/DspreadOrg/android/blob/master/QPOS-Android-SDK-Userguid-en-detail.pdf)** : Please check the link to know the specific API reference involved in SDK.

## Support
For issues and questions, please visit our [community](https://github.com/orgs/DspreadOrg/discussions) or contact Dspread support.