# SmsVerifyCatcher

[![codebeat badge](https://codebeat.co/badges/0d0a3e88-6da8-4e43-a0fc-999af604d0b4)](https://codebeat.co/projects/github-com-stfalcon-studio-smsverifycatcher-master) [ ![Download](https://api.bintray.com/packages/bevzaanton/maven/SmsVerifyCatcher/images/download.svg) ](https://bintray.com/bevzaanton/maven/SmsVerifyCatcher/_latestVersion)

![alt tag](http://i.imgur.com/7Kzzk5z.gif)

A library for implementing interception of SMS with a verification code using a few lines of code.

### Who we are
Need iOS and Android apps, MVP development or prototyping? Contact us via info@stfalcon.com. We develop software since 2009, and we're known experts in this field. Check out our [portfolio](https://stfalcon.com/en/portfolio) and see more libraries from [stfalcon-studio](https://stfalcon.com/en/opensource).

### Download

Download via Gradle:
```gradle
compile 'com.github.stfalcon:smsverifycatcher:0.3.2'
```

or Maven:
```xml
<dependency>
  <groupId>com.github.stfalcon</groupId>
  <artifactId>smsverifycatcher</artifactId>
  <version>0.3.2</version>
  <type>pom</type>
</dependency>
```

### Usage

Add permissions to AndroidManifest.xml:
```xml
  <uses-permission android:name="android.permission.RECEIVE_SMS" />
  <uses-permission android:name="android.permission.READ_SMS" />
```
Init SmsVerifyCatcher in method like onCreate activity:
```java
    smsVerifyCatcher = new SmsVerifyCatcher(this, new OnSmsCatchListener<String>() {
        @Override
        public void onSmsCatch(String message) {
            String code = parseCode(message);//Parse verification code
            etCode.setText(code);//set code in edit text
            //then you can send verification code to server
        }
    });
```
Override activity lifecicle methods:
```java
    @Override
    protected void onStart() {
        super.onStart();
        smsVerifyCatcher.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        smsVerifyCatcher.onStop();
    }

    /**
     * need for Android 6 real time permissions
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        smsVerifyCatcher.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
```

You can set phone number filter:
```java
    smsVerifyCatcher.setPhoneNumberFilter("777");
```
or set message filter via regexp:
```java
   smsVerifyCatcher.setFilter("<regexp>");
```
That's all! 
Take a look at the [sample project](sample) for more information

### License 

```
Copyright 2017 stfalcon.com

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


[sample]: <https://github.com/stfalcon-studio/SmsVerifyCatcher/tree/master/sample>



