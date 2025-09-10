## <a id="dma_support"> Send consent for DMA compliance
For a general introduction to DMA consent data, see [here](https://dev.appsflyer.com/hc/docs/send-consent-for-dma-compliance).<be>
The AppsFlyer SDK offers two alternative methods for gathering consent data:<br>
- **Through a Consent Management Platform (CMP)**: If the app uses a CMP that complies with the [Transparency and Consent Framework (TCF) v2.2 protocol](https://iabeurope.eu/tcf-supporting-resources/), the SDK can automatically retrieve the consent details.<br>
  OR<br>
- **Through a dedicated SDK API**: Developers can pass Google's required consent data directly to the SDK using a specific API designed for this purpose.
### Use CMP to collect consent data
A CMP compatible with TCF v2.2 collects DMA consent data and stores it in <code>SharedPreferences</code>. To enable the SDK to access this data and include it with every event, follow these steps:<br>
<ol> 
  <li> Create AppsFlyer plugin object <code>val appsFlyerDestination = AppsFlyerDestination(this, true) in the Activity class</code>
  <li> Call <code>appsFlyerDestination.enableTCFDataCollection = true</code> to instruct the AppsFlyer SDK to collect the TCF data from the device. 
  <li> Call <code>appsFlyerDestination.startAppsFlyerManually = true</code>. <br> This will allow us to delay the Conversion call in order to provide the SDK with the user consent. 
  <li> In the <code>Activity</code> class, use the CMP to decide if you need the consent dialog in the current session.
  <li> If needed, show the consent dialog, using the CMP, to capture the user consent decision. Otherwise, go to step 7. 
  <li> Get confirmation from the CMP that the user has made their consent decision, and the data is available in <code>SharedPreferences</code>.
  <li> Call <code>AppsFlyerLib.getInstance().start(this)</code>
</ol> 

#### Activity class
```kotlin
class MainActivity: Activity() {

  private val consentRequired = true

  override fun onCreate(savedInstanceState: Bundle?) {
      super.onCreate(savedInstanceState)
    
      setContentView(R.layout.activity_main)
    
      val analytics = Analytics("dev_key", applicationContext) {
        this.flushAt = 3
        this.trackApplicationLifecycleEvents = true
      }

      val appsFlyerDestination = AppsFlyerDestination(this, true)
      if (consentRequired) {
          appsFlyerDestination.enableTCFDataCollection = true
          appsFlyerDestination.startAppsFlyerManually = true
          initConsentCollection()
      }
      analytics.add(plugin = appsFlyerDestination)
  }
  
  private fun initConsentCollection() {
    // Implement here the you CMP flow
    // When the flow is completed and consent was collected 
    // call onConsentCollectionFinished()
  }

  private fun onConsentCollectionFinished() {
    AppsFlyerLib.getInstance().start(this)
  }
}
```

### Manually collect consent data
If your app does not use a CMP compatible with TCF v2.2, use the SDK API detailed below to provide the consent data directly to the SDK.
<ol> 
  <li> Create AppsFlyer plugin object <code>val appsFlyerDestination = AppsFlyerDestination(this, true) in the Activity class</code> 
  <li> In the <code>Activity</code> class, determine whether the GDPR applies or not to the user.<br> 
    - If GDPR applies to the user, perform the following:  
      <ol>
        <li> Call <code>appsFlyerDestination.startAppsFlyerManually = true</code>. <br> This will allow us to delay the Conversion call in order to provide the SDK with the user consent.
        <li> Given that GDPR applies to the user, determine whether the consent data is already stored for this session. 
            <ol> 
              <li> If there is no consent data stored, show the consent dialog to capture the user consent decision. 
              <li> If there is consent data stored, continue to the next step. 
            </ol> 
        <li> To transfer the consent data to the SDK, create an object called AppsFlyerConsent with the following optional parameters:<br> 
          - <code>isUserSubjectToGDPR</code> - Indicates whether GDPR applies to the user.<br>
          - <code>hasConsentForDataUsage</code> - Indicates whether the user has consented to use their data for advertising purposes.<br>
          - <code>hasConsentForAdsPersonalization</code> - Indicates whether the user has consented to use their data for personalized advertising purposes.<br>
          - <code>hasConsentForAdStorage</code> - Indicates whether the user has consented to store or access information on a device.<br>
        <li> Call <code>AppsFlyerLib.getInstance().setConsentData()</code> with the <code>AppsFlyerConsent</code> object.    
        <li> Call <code>AppsFlyerLib.getInstance().start(this)</code>. 
      </ol><br> 
    - If GDPR does not apply to the user, set <code>isUserSubjectToGDPR</code> to false and the rest of the parameters must be null. See example below:
      <ol> 
        <li> Create an <code>AppsFlyerConsent</code> object:<br> <code>val nonGdprUser = AppsFlyerConsent(false, null, null, null)</code>
        <li> Call <br><code>AppsFlyerLib.getInstance().setConsentData(nonGdprUser)</code>  
     </ol>
  <li> Call <code>analytics.add(plugin = appsFlyerDestination)</code>  <br> 


#### Activity class
```kotlin
class MainActivity: Activity() {

  private val consentRequired = true

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val analytics = Analytics("dev_key",applicationContext) {
      this.flushAt = 3
      this.trackApplicationLifecycleEvents = true
    }

    val appsFlyerDestination = AppsFlyerDestination(this, true)
    if (consentRequired) {
      appsFlyerDestination.startAppsFlyerManually = true
      presentConsentCollectionDialog()
    } else {
      val nonGdprUser = AppsFlyerConsent(false, null, null, null)
      AppsFlyerLib.getInstance().setConsentData(nonGdprUser)
    }
    analytics.add(plugin = appsFlyerDestination)
  }

  private fun presentConsentCollectionDialog() {
    // When the flow is completed and consent data was collected 
    // call onConsentCollectionFinished(consent)
  }

  private fun onConsentCollectionFinished(consent: AppsFlyerConsent) {
    AppsFlyerLib.getInstance().setConsentData(consent)
    AppsFlyerLib.getInstance().start(this)
  }
}
```
</ol> 

