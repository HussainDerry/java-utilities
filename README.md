# Java Utilities Module

[![Codacy Badge](https://api.codacy.com/project/badge/Grade/288fbb060ade4282821af1a63c10de64)](https://www.codacy.com/app/hussain.derry/java-utilities?utm_source=github.com&utm_medium=referral&utm_content=HussainDerry/java-utilities&utm_campaign=badger)  [![Build Status](https://travis-ci.org/HussainDerry/java-utilities.svg?branch=master)](https://travis-ci.org/HussainDerry/java-utilities)


Contains helper classes that I find useful every now and then.

## Classes Summary

### ImageCompressionUtils

Used to compress image files to JPEG format using the desired quality for the compressed image.

#### Sample Usage

```java
// Compression quality (value >= 0 and <= 1)
final float quality = 0.35f;

// Compressing to bytes from source image bytes
byte[] compressed = ImageCompressionUtils.compressImage(sourceBytes, quality);
```

### ImageCompressor

A Thread-Safe Object-Oriented wrapper for the ImageCompressionUtils utility class with Base64 and method chaining support.

#### Sample Usage

```java
// Creating a new instance and setting the compressed image quality
ImageCompressor mImageCompressor = new ImageCompressor().setCompressedImageQuality(0.75f);

// Compressing an image to byte array
byte[] compressed = mImageCompressor.setSourceImage(sourceBytes).compressImageToByteArray();

// Modifying the compressed image quality, and compressing a new image to Base64
String compressedBase64 = mImageCompressor.setCompressedImageQuality(0.35f)
					  .setSourceImage(sourceBytesTwo)
  					  .compressImageToBase64();
```

### ConcurrentCache

Cache implementation with a periodic memory clean up process for objects that haven't been accessed for a specified period of time.

#### Sample Usage

```java
long cleanUpInterval = 100; // milliseconds
long objectTTL = 2000; // milliseconds
int cacheSize = 25;

private ConcurrentCache<Integer, String> mCache = new ConcurrentCache<>(objectTTL, cleanUpInterval, cacheSize);
```

### PBKDF2Helper

Wrapper class to help manipulate PBKDF2 parameters and generate keys.

#### Sample Usage

```java
// Using the builder
PBKDF2Helper mHelper = new PBKDF2Helper.Builder(KeySize.KEY_256)
                .iterations(Iterations.MEDIUM)
                .saltSize(SaltSize.SALT_128)
                .build();

// Generating a key
byte[] key = mHelper.createKeyFromPassword(PASSWORD);

// Getting configurations for storage
String config = mHelper.getPbkdf2Configurations();

// Creation using the configurations string
PBKDF2Helper mHelper = new PBKDF2Helper.Builder(config).build();
```

### FileEncryptorAES

Used to encrypt files using AES with PBKDF2.

#### Sample Usage

```java
// Using factory method for creation
FileEncryptorAES mEncryptor = FileEncryptorAES.createEncryptorWithHighSecurityParams(PASSWORD);

// Setting the progress monitor
mEncryptor.setProgressMonitor((int progress) -> System.out.println());

// Encryption
BufferedInputStream mInputStream = new BufferedInputStream(new FileInputStream(mSourceFile));
BufferedOutputStream mOutputStream = new BufferedOutputStream(new FileOutputStream(mTargetFile));
mEncryptor.encrypt(mInputStream, mOutputstream);
```

### FileDecryptorAES

Used to decrypt files encrypted with FileEncryptorAES.

#### Sample Usage

```java
// Creation
FileDecryptorAES mDecryptor = new FileDecryptorAES(PASSWORD);

// Setting the progress monitor
mDecryptor.setProgressMonitor((int progress) -> System.out.println());

// Decryption
BufferedInputStream mInputStream = new BufferedInputStream(new FileInputStream(mSourceFile));
BufferedOutputStream mOutputStream = new BufferedOutputStream(new FileOutputStream(mTargetFile));
mDecryptor.decrypt(mInputStream, mOutputStream);   
```

### Secure Preferences

Provides an AES encryption layer over the `java.util.prefs.Preferences` class.

#### Sample Usage

```java
// Setting up the prefs
SecurePreferences mPreferences = new SecurePreferencesImpl("test-node", "pa$$word");

// Storing a string value
String str = "test-data";
mPreferences.putString("string", str);

// Getting the value
Optional<String> mOptional = mPreferences.getString("string");
if(mOptional.isPresent()){
    String loaded = mOptional.get();
}
```

### CloneUtils

Used to deep clone objects that implements the Serializable interface. (Any objects used by the class must also implement Serializable).

#### Sample Usage 

```java
SomeObject clone = CloneUtils.deepClone(origin, SomeObject.class);
```


### LuhnChecker  

 Used to check card numbers using the Luhn formula.

#### Sample Usage

```java
boolean isValid = LuhnChecker.checkNumberValidity("4532136548631895");
```


## Developed By

- Hussain Al-Derry

   **Email** - [hussain.derry@gmail.com](mailto:hussain.derry@gmail.com)


## License 

```latex
Copyright 2017 Hussain Al-Derry

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

