
### About `Wenjiu`

Wenjiu is an Android app developed for paper COMP548.
It is mainly to demonstrate the abundance of UI components, Networking and energy consumption.

---

This App is used to ask and answer questions related to Chinese Moxibustion. The idea is from the real requirements of my friend in China.

### Features

- Login with real account
- Ask questions
- Answer questions
- Show question list and question detail
- Upvote / downvote an answer
- History activities in the site

### Technical points

#### Requirement checklist

* Totally developed in __`Kotlin`__
* At least 5 different API functions: 
  - Login and fetch access token
  - Query question list, Query question detail
  - Ask question, Answer question
  - Upvote / downvote
  - Activity stream
* a variety of interface uses: 
  - RecyclerView, CardView, SwipeRefreshLayout
  - BottomSheetFragment, BottomNavigationView
  - ViewPager, TimelineView, MarkdownView
* preferences framework: I use preferences to store user session -> access token and current logined user infomation
* reduce energy consumption: refer to my report

#### Test environment

- SDK API 25, minimum SDK 24
- Tested on Google Pixel

#### Notice

- All the backend API is under development, so the functionalities are not complete so far. 
- Backend server is deployed in China and has no CDN, so the connection speed can be slow. Keep tuned, thank.
If you fail to connect or the connection is unstable, please contact richd.yang@gmail.com

### Screenshots

<img src="Screenshot1.png" width="300" />
<img src="Screenshot2.png" width="300" />
<img src="Screenshot3.png" width="300" />
<img src="Screenshot4.png" width="300" />
<img src="Screenshot5.png" width="300" />
<img src="Screenshot6.png" width="300" />
<img src="Screenshot7.png" width="300" />
<img src="Screenshot8.png" width="300" />
<img src="Screenshot9.png" width="300" />
<img src="Screenshot10.png" width="300" />
<img src="Screenshot11.png" width="300" />
<img src="Screenshot12.png" width="300" />
