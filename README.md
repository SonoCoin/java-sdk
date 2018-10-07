# SonoCoin Android SDK

************
Introduction
************

SonoCoin is a decentralized blockchain platform with a native crypto currency based on proof of stake (PoS). As such, there exist a variety of applications surrounding the SonoCoin ecosystem.

## What is it
This is source code of Android library (in the form of AAR file) with implementation of SonoCoin on Android ecosystem. 


## Use AAR
- Copy folder `sonocoin-sdk` to your project folder.
- Open your `build.gradle` and add this line to your dependencies:
`implementation project(':sonocoin-sdk')`

So your block will be like that:
```gradle
dependencies {
    // Hint: You could use the folder with source code instead of AAR
    implementation project(':sonocoin-sdk')
    implementation 'com.android.support:appcompat-v7:26.1.0'
    implementation 'com.android.support.constraint:constraint-layout:1.1.3'
    implementation 'com.android.support:design:26.1.0'
    implementation 'com.github.wendykierp:JTransforms:3.1'
    implementation 'junit:junit:4.12'
    implementation 'com.android.support.test:runner:1.0.2'
    implementation 'com.android.support.test.espresso:espresso-core:3.0.2'
    implementation "com.google.code.gson:gson:2.2.4"
    implementation 'com.amitshekhar.android:android-networking:1.0.0'
    implementation 'com.amitshekhar.android:jackson-android-networking:1.0.0'
    implementation 'com.github.joshjdevl.libsodiumjni:libsodium-jni-aar:1.0.6'
  ...
}
``` 

- Sync project
- Build -> Make Project
- Run example on device

## Examples
Examples of using is placed at folder `/app/src/androidTest/java/io/sonocoin/sdktestapplication`

Some example of using:
```java
    // Read from Json file
    String filename = ...;
    io.sonocoin.sdk.Types.Item item =
        io.sonocoin.sdk.Sound.SonoJsonSerializer.loadItemFromFile(filename);
    io.sonocoin.sdk.Types.Coin coin = item.GetCoinFromRemoteNode();
    
    // Write to Json file
    io.sonocoin.sdk.Sound.SonoJsonSerializer.saveCoinToFile(coin, filename);
```

## Building AAR
Source code of AAR file included, and new AAR file will be placed to folder `/sdk/build/outputs/aar` on compilation.

