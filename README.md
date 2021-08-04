# WithrunAndroid
Running app android kotlin project


		○ 프로젝트명 : WithRun (위드런)


		○ 프로젝트 소개 :  GPS 를 이용해 실시간으로 러닝 기록을 비교하며 경쟁하거나 또는 러닝 활동을 기록하여 다른사람과 공유하는 어플


		○ 프로젝트 개발 목적 : '기록 경쟁' 이라는 강력한 동기부여로 성취욕과 정신력 향상을 목표로 하고, 
				     초심자도 쉽게 러닝의 재미를 붙일 수 있게 하기위해 기획


		○ 주요 기능
		-러닝 경쟁 : GPS 를 이용한 러닝 활동 추적 및 경쟁순위 실시간 변동
		-실시간 음성 안내 : 러닝 중 현재 러닝 기록( 1km 간격으로 이동 시 알림 : 운동시간, 페이스, 거리), 순위 변동 시 현재순위, 러닝 종료시 음성 알림
		-채팅 : 인원 모집 중 룸안의 참가 신청 중인 인원과 실시간 채팅
		-랭킹 : 주간별 기록을 통한 순위 정렬
		-팔로우 : 팔로우 유저의 경기 활동 내역을 공유
		-개인기록 : 경기 기록 보기 (월간 정렬) 
		-지도 : GPS 를 이용하여 경주 구간을 polyline으로 표기 및 경주가 끝날 시 경주 구간이 표기된 지도 이미지를 저장 (구글 맵)


		○ 선택 접근 권한
		-위치 : 실외 러닝 경로를 확인하기 위해 사용
		-카메라 : 프로필 사진을 등록하거나 업데이트 할 시 사진을 찍기 위해 사용
		-저장공간 : 프로필 사진을 등록하거나 업데이트 할 시 사진, 미디어 및 파일에 접근하기 위해 사용
		
		
		○ 사용언어
		Front-End : Kotlin, Java
		Back-End : NodeJS ( HTTP ), Java ( TCP )
		
    
		○ 개발 환경
		Server : AWS EC2 ubuntu
		Client : Android Studio
		Database : Amazon RDS MariaDB
		
    
		○ 통신 프로토콜
		HTTP, TCP/IP, SSH, SFTP, SMTP
		
		
		○ 라이브러리
		googleMap API
		com.google.android.gms.location
		hdodenhof.circleimageview
		jetbrains.coroutines
		okhttp3
		bumptech.glide
		firebase-messaging
		android.tts
		java.socket 
		
    
		○ Database ERD
[WithRun_database_dbdiagram.pdf](https://github.com/Kimanna/WithrunAndroid/files/6933277/WithRun_database_dbdiagram.pdf)
[WithRun_database_dbdiagram](https://github.com/Kimanna/WithrunAndroid/files/6933277/WithRun_database_dbdiagram.png)

		
				
		○ 기능 시연 영상
		
		1.회원가입, 로그인, 비밀번호 찾기 (변경), 프로필 등록 및 변경
		-회원가입 및 이메일 인증 ( ~ 01:06 )
		-회원가입 시 프로필 등록 ( 01:07 ~ 01:35 )
		-로그인, 자동로그인 ( 01:36 ~ 01:50 )
		-프로필 변경 ( 01:51 ~ 02:20 )
		-비밀번호 찾기 및 재 설정 ( 02:25 ~ 03:20 ) 
		-회원탈퇴 ( 03:20 ~ )
		
[![image](https://user-images.githubusercontent.com/69760221/128225957-3cc326a9-afdb-4d1e-bce0-84c492acbddb.png)](https://youtu.be/bzdoSOX4IIQ)


		2.경기 룸 만들기 (+ 참여하기), 초대, 채팅
		-경기 룸 만들기 ( ~ 00:25 )
		-현재 방에 참여하고있지 않은 유저에게 초대 알림 발송 ( 00:26 ~ 00:55 )
		-경기 룸 참여하기, 참여 취소하기 
		-참여중인 룸 멤버끼리 채팅 ( 00:56 ~ 02:44 )
		-경기 룸 목록 페이징 ( 02:45 ~ 03:35 )
		-참여중인 경주룸 보기 ( 03:45 ~ 03:50 )
		-경주가 종료된 룸 삭제 ( 03:51 ~ 04:25 )
		-초대받은 유저의 알림 상태창에 초대메시지 저장 및 삭제 ( 04:25 ~ )
		
[![image](https://user-images.githubusercontent.com/69760221/128225973-90220ae2-4122-41e0-8d32-42ba9683b091.png)](https://youtu.be/5gzkIHLfPmE)


		3.팔로우 ( +알림 상태창), 상대프로필 보기
		-팔로우 신청과 알림발송 혹은 팔로우 취소 
		-팔로우 수락 및 팔로워 유저 삭제
		-팔로우 유저의 최근30일동안의 경기 내역 통계 확인 
		-팔로우 유저 추천 및 팔로우신청 ( ~ 01:39 )
		( 평균페이스가 유사한 사용자를 상위배치, 이름순 정렬 )
		-팔로우 유저의 최근 활동 내역 보기 또는 숨기기 ( 01:40 ~ )
		

[![image](https://user-images.githubusercontent.com/69760221/128225981-826d035a-ff66-42c9-9821-56e1cf0bbc9d.png)](https://youtu.be/k3w7VbHYL-U)


		4.러닝 경기
		-경기 시작 시간 10분전 & 경기 시작 시 알림 ( ~ 01:35 )
		-경주 중엔 앱이 백그라운드 상태에서도 경기시간 및 이동거리를 상태창 으로 실시간 업데이트 ( 01:36 ~ 02:15 )
		-유저의 이동 경로를 지도 상에 폴리라인으로 실시간 표시 ( 02:15 ~ )
		-유저 상태 (입장전/러닝중/경주완료/미완주) 혹은 경주 남은거리가 짧은 순으로 유저 정렬
		-1km 간격으로 경주기록 ( 경기시간, 총 경주거리(km), 페이스 ) 음성알림
		-순위 변동 혹은 경주 완료 시 음성 알림 ( background 상태에서도 알림 ) ( 02:15 ~ 04:55 ) 
		-완주 & 경기 포기시 경주기록과 이동경로가 포함된 지도를 함께 저장 ( 04:56 ~ )
		

[![image](https://user-images.githubusercontent.com/69760221/128225987-9a3b3483-7336-424d-9db7-ae3c955a9fe7.png)](https://youtu.be/qOEmoMySWUc)


		5.설정
		-푸시알림, 이메일알림, 초대허용, 기록공개 on/off 설정 ( ~ 01:28 )
		-고객지원, 서비스 이용약관, 개인정보 처리방침, 서비스 이용 동의, 버전 등을 확인 ( 01:29 ~ 01:50 )
		-로그아웃 ( 01:55 ~ )
	

[![image](https://user-images.githubusercontent.com/69760221/128225993-c1da2f46-6c5d-404d-9bbb-efdff0a2e6d1.png)](https://youtu.be/JFmGLzSQyCE)


		6.전체 유저 주간 랭킹, 내 경기 내역 보기 (월간 정렬), 최근 경기 활동 내역  
		-내 경기 내역 보기 ( 월간 정렬 ) ( ~ 00:36 )
		-전체 유저의 기록 주간 랭킹 ( 00:37 ~ 01:00 )
		-해당 유저와 팔로잉 유저의 최근 활동 경기 보기 및 삭제 ( 01:00 ~ )


[![image](https://user-images.githubusercontent.com/69760221/128226006-9d2bf9b8-e080-4bce-a48a-82ff9d9337fc.png)](https://youtu.be/R7EMEKn2pZs)
