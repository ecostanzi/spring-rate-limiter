Flydown
===

[![Build Status](https://travis-ci.org/encos/spring-rate-limiter.svg?branch=master)](https://travis-ci.org/encos/flydown)


**A Spring rate limiter**

### The Goal
Flydown provides a rate limiter based on the AOP technology. It mainly relies on in-memory data store to efficiently rate all the potential threats for your system. 

With Flydown you'll be able to limit:
* the principal obtained by the securityContextHolder
* any parameter contained in the signature of the annotated method
* any variable you want to insert in the *flydown request context*

Let's say you have to manage the public APIs of a social network. Of course you want to limit any malicious behaviour. 
Add these few lines to our spring xml configuration file
```
<bean id="memoryRateCache" class="org.encos.flydown.limiters.cache.impl.InMemoryRateCache"/>

<bean class="org.encos.flydown.Flydown">
    <property name="rateCache" ref="memoryRateCache"/>
</bean>
```
#### Principal Rating
You don't want a user to insert more than 5 comments in one minute. If this behaviour is detected the user has to be stopped temporarily from inserting comments in the platform. Let's give him a 5 minutes break. Here's what you can do:
```
@RequestRate(value = FlydownIdentifier.PRINCIPAL,
        max = 5, range = 60000,
        suspendFor = 36000)
public void commentPost(int postId, String comment) {
  // the principal does something
}
```

#### Parameter Rating
You don't want a user to receive be sent more than 1 SMS a minute if he/her forgets his credentials. The same number can't receive more than 1 sms a minute. If a second request comes into the system in this range, all the SMS to this number are blocked for 5 minutes.
```
@RequestRate(value = FlydownIdentifier.PARAM, paramIndex = 0,
        max = 1, range = 60000,
        suspendFor = 36000))
public void sendSms(String phoneNumber){
  //send an sms to the phone number
}
```

#### Flydown Context Rating
Let's say you don't want to learn how to use nginx and you want to set up a (temporary) IP rating limiting the access to one of you APIs.
```
@Autowired
IRateCache rateCache;

@RequestRate(value = FlydownIdentifier.CONTEXT_VAR, contextKey = "IP")
public void doSomething(HttpRequest request) {
  String currentIp = MyUtils.getIp(request);
  rateCache.addToContext("IP", currentIp);
  //do something
}
```

#### Rating Exception
Requests might not be the only thing you want to limit. A malicious behaviour can be detected and announced also by a java exception. 

```
@ExceptionRate(value = FlydownIdentifier.PRINCIPAL,
        max = 1, range = 60000,
        suspendFor = 36000, exception=BadLanguageException.class)
public void commentPost(int postId, String comment) {
  // the principal does something
}
```

#### Default values

You might also want to set default values for most of your Request/Exception Rate, this can be done through the flydown properties:

```
<bean class="org.encos.flydown.Flydown">
    <property name="rateCache" ref="memoryRateCache"/>
    <property name="flydownProperties">
        <props>
            <prop key="flydown.requests.limit">10</prop>
            <prop key="flydown.interval.time">10000</prop>
            <prop key="flydown.suspension.time">36000</prop>
        </props>
    </property>
</bean>
```

So that your annotations become more readable:

```
@RequestRate(value = FlydownIdentifier.PRINCIPAL)
public void commentPost(int postId, String comment) {
  // the principal does something
}
    
    
```


### Available Caches
* InMemoryRateCache is a dummy implementation of a key/value store 
* RedisRatingCache the cache implementation relying on redis

To be implemented
* EhCacheRatingCache the cache implementation relying on ehcache

Any others? There's just an interface to implement :)

### What's missing?

Mainly *time*! Any help or suggestions are welcome!
