# YeetClock
This is a personal project I made that is pretty cool alarm clock that also controls LED lights, shows the weather forecast, your 3D print's progress and the status of your self hosted self hosted services while also providing a mobile app so that you can control the led lights to your preference.

## Why did I do this?

Well, since in Turkey we don't set hour clocks 1 hour ahead anymore, I have been having issues getting up for school because I always have to wake up in pitch black. So I decided to make an alarm clock that automatically turns on some LED lights imitating sunset. But since I didn't have an RTC module at the time of starting this project, I used a NodeMCU v3.0 that uses NTP. Because my alarm clock was going to connect to the internet anyways, I decided to overengineer the hell out of it and added a bunch of features. Even though some of its features like self hosted server status and octoprint status are pretty much useless to other people. I though they were pretty cool! Of course you are free not to use it :D. 

## This code sucks ass!
Yeah I know. As I said it is side project that I did in pretty much a week during the quarantine. Obviously I would love to refactor it and make it pretty but currently I am studying for my A Levels and don't have much free time. Once I can find some time to spend on non-crucial business, I promise I will getto it.

###TODOs

- Add a proper RTC module to the system.
- Integrate a bigger lcd screen.
- Make the README less cringey
- Refactor the code properly 
