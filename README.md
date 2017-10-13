# Java Utilities Module

Contains helper classes that I find useful every now and then.

## Classes Summary

### `ImageCompressionUtils`

Used to compress image files to JPEG format using the desired quality for the compressed image.

#### Sample Usage

```java
// Compression quality (value >= 0 and <= 1)
final float quality = 0.35f;

// Compressing to bytes from source image bytes
byte[] compressed = ImageCompressionUtils.compressImage(sourceBytes, quality);

// Compressing to bytes from Base64 encoded source image bytes
byte[] compressed = ImageCompressionUtils.compressImage(sourceBase64, quality);

// Compressing to Base64 from source image bytes
String compressedBase64 = ImageCompressionUtils.compressImageBase64(sourceBytes, quality);

// Compressing to Base64 from Base64 encoded source image bytes
String compressedBase64 = ImageCompressionUtils.compressImageBase64(sourceBase64, quality);
```  


### `ConcurrentCache`

Cache implementation with a periodic memory clean up process for objects that haven't been accessed for a specified period of time.

#### Sample Usage

```java
long cleanUpInterval = 100; // milliseconds
long objectTTL = 2000; // milliseconds
int cacheSize = 25;

private ConcurrentCache<Integer, String> mCache = new ConcurrentCache<>(objectTTL, cleanUpInterval, cacheSize);

```  


### `CloneUtils`

Used to deep clone objects that implements the Serializable interface. (Any objects used by the class must also implement Serializable).

#### Sample Usage 

```java
SomeObject clone = CloneUtils.deepClone(origin, SomeObject.class);
```  


### `LuhnChecker`  

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
