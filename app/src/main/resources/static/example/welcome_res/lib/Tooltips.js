//////////////// Retrieve tooltip messages for client libs

var tooltipMessages = {
    javascript_client:"To connect your HTML or HTML5 app to Lightstreamer Server, you just need to integrate a JavaScript lib, which is provided together with extensive documentation and example code. This same client API can be used within PhoneGap and several hybrid platforms as well",
    dotnet_client:"To add real time information to your Unity 3d project through Lightstreamer Server, you just need to integrate a .NET lib, which is provided together with extensive documentation and example code",
    pcl_client:"To connect your .NET application to Lightstreamer Server, you just need to integrate a .NET PCL lib, which is provided together with extensive documentation and example code. The Portable Class Library (PCL) works seamlessly with dekstop .NET applications, as well as Windows Phone and WinRT apps. This flag apply also to the old client libraries: .NET, WinRT, and Windows Phone",
    android_client:"To connect your Android native application to Lightstreamer Server, you just need to integrate a Java lib, which is provided together with extensive documentation and example code. This same client API can be used within BlackBerry10 projects as well",
    blackberry_client:"To connect your BlackBerry 7 native application to Lightstreamer Server, you just need to integrate a Java lib, which is provided together with extensive documentation and example code. If you are targeting BlackBerry 10 apps, you can choose among the Web Client API, Android Client API, and Flex Client API instead",
    flex_client:"To connect your Flex and AIR applications to Lightstreamer Server, you just need to integrate an ActionScript lib, which is provided together with extensive documentation and example code. This same client API can be used within BlackBerry10 projects as well",
    ios_client:"To connect your Swift or Objective-C application for iOS (iPhone and iPad) to Lightstreamer Server, you just need to integrate a specific iOS lib, which is provided together with extensive documentation and example code",
    javame_client:"To connect your Java midlet to Lightstreamer Server, you just need to integrate a Java lib, which is provided together with extensive documentation and example code",
    javase_client:"To connect your Java SE application to Lightstreamer Server, you just need to integrate a Java lib, which is provided together with extensive documentation and example code",
    nodejs_client:"To connect your Node.js server to Lightstreamer Server, so that Node acts as a Lightstreamer client, you just need to integrate a JavaScript lib, which is provided together with extensive documentation and example code.",
    osx_client:"To connect your Swift or Objective-C application for Mac to Lightstreamer Server, you just need to integrate a specific macOS lib, which is provided together with extensive documentation and example code",
    silverlight_client:"To connect your Silverlight application to Lightstreamer Server, you just need to integrate a Silverlight lib, which is provided together with extensive documentation and example code",
    windows_phone_client:"To connect your Windows Phone native application to Lightstreamer Server, you just need to integrate a .NET lib, which is provided together with extensive documentation and example code",
    winrt_client:"To connect your Windows 8 application to Lightstreamer Server, you just need to integrate a WinRT lib, which is provided together with extensive documentation and example code",
    generic_client:"You can develop Lightstreamer clients in any language by implementing a well documented network protocol, called TLCP (Text Lightstreamer Client Protocol). TLCP is based on HTTP and WebSockets and it is easy to develop a cient that implements it",                    
    tvos_client:"To connect your Swift or Objective-C application for Apple TV to Lightstreamer Server, you just need to integrate a specific tvOS lib, which is provided together with extensive documentation and example code",
    else:"To connect your client application to Lightstreamer Server, you just need to integrate the provided client lib, which is provided together with extensive documentation and example code"
};

function  getTooltipMessage(lib) {
    var tmp =  tooltipMessages[lib];
    
    if (tmp != null ) { 
        return tmp;
    } else {
        return tooltipMessages["else"];
    }
        
}
    
