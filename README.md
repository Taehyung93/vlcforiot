# 개요


### 해당 어플의 기능
```
1. VLC Library for Android 를 사용해 RTSP Streaming 을 하며,

2. Naver Map API 를 통해 현재 위치를 보여주고 현재 위치를 MQTT 로 Host PC 에 전송한다.
```
### 만든 이유
```
당장 이 어플 하나로 위의 기능이상의 무엇인가를 할 수는 없다.
대신,

1. Home IOT 플랫폼에 들어가는 월패드를 위한 기능 테스트와 
2. 드론에서 송출한 영상을 어플에서 받아보며 지도를 통해 조종하는 어플을 위한 기본 기능 테스트를 위해 만들었다.
```
### 의의
```
이 소스를 보는 분들에게는 VLC 최신 라이브러리를 따로 컴파일하지 않고 바로 사용가능한 점과

매우 간결하게 코드를 구성하여 누구나 살펴보면 쉽게 따라 할 수 있도록 RTSP 를 제외한 동영상 재생 기능등은 뺐다.
```

# 참고

### VLC 버퍼관련 이슈
참고 링크: https://code.videolan.org/videolan/vlc-android/issues/548
```
아래 옵션을 주면 네트워크 버퍼 타임을 조절할 수 있다. 조정하지 않으면 default 는 1500ms 이다.
args.add("--network-caching=0"); 
위와 같이 0 으로 옵션을 조절하면 좀 더 영상이 실시간으로 되어야 하지만
아마 네트워크 불안정으로 인해 자주 끊기고 실제로 빨라진 체감이 거의 않든다.

그리고 기본적으로 기본 PC 의 VLC player 에 비해 자주 끊김이 있다.
```

### VLC android 최신버전 import 하는 방법
```
implementation 'org.videolan.android:libvlc-all:<VERSION>'

find the string "libvlcVersion" below link to get the latest version.

https://github.com/videolan/vlc-android/blob/master/build.gradle#L33
```

### Naver Map API 삽입 관련 
```
https://navermaps.github.io/android-map-sdk/guide-ko/

네이버 공식 개발자 문서인 위 링크만 보고 따라가도 완전히 잘 돌아가지만, 개인적으로는 몇 시간 헤맸다.

가장 중요한 개념은 아래 코드를 통해 네이버 지도 객체를 생성하고,
mapFragment.getMapAsync(this);
생성이되면, 콜백함수인 아래 함수가 동작을 해서 이 함수안에 지도에서 쓸 기능을 모두 넣으면 된다는 것이다.
public void onMapReady(@NonNull NaverMap naverMap)
```
## 추후 개발

1. 지도 기능 및 현재 위치 전송 기능 개발
2. 여기에 VLC 대신 ffmpeg 으로 RTSP 통신 가능하도록 개발
