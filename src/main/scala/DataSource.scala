import java.net.URL

import com.sforce.soap.partner.Connector
import com.sforce.ws.ConnectorConfig
import io.prediction.controller.PDataSource
import io.prediction.controller.EmptyEvaluationInfo
import io.prediction.controller.EmptyActualResult
import io.prediction.controller.Params
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import grizzled.slf4j.Logger
import org.apache.commons.httpclient.{HttpClient, NameValuePair}
import org.apache.commons.httpclient.methods.GetMethod
import org.json4s._
import org.json4s.jackson.JsonMethods._

case class DataSourceParams(salesforceUsername: String, salesforcePassword: String) extends Params

class DataSource(val dataSourceParams: DataSourceParams) extends PDataSource[TrainingData, EmptyEvaluationInfo, Query, EmptyActualResult] {

  @transient lazy val logger = Logger[this.type]

  // use the SOAP API so we don't need an OAuth app
  private val loginInfo: (String, String) = {
    val loginUrl = "https://login.salesforce.com/services/Soap/u/38.0"

    val loginConfig = new ConnectorConfig()
    loginConfig.setAuthEndpoint(loginUrl)
    loginConfig.setServiceEndpoint(loginUrl)
    loginConfig.setManualLogin(true)

    val partnerConnection = Connector.newConnection(loginConfig)
    val loginResult = partnerConnection.login(dataSourceParams.salesforceUsername, dataSourceParams.salesforcePassword)

    val serverHost = new URL(loginResult.getServerUrl).getHost

    (loginResult.getSessionId, serverHost)
  }

  override def readTraining(sc: SparkContext): TrainingData = {
    val (accessToken, hostname) = loginInfo

    val httpClient = new HttpClient()

    val getFavorites = new GetMethod(s"https://$hostname/services/data/v37.0/query")
    getFavorites.addRequestHeader("Authorization", "Bearer " + accessToken)
    val query = new NameValuePair("q", "SELECT Property__c, User__c FROM Favorite__c")
    getFavorites.setQueryString(Array(query))

    httpClient.executeMethod(getFavorites)

    val json = parse(getFavorites.getResponseBodyAsStream)

    val favorites = for {
      JArray(favorites) <- json \ "records"
      JObject(favorite) <- favorites
      JField("Property__c", JString(propertyId)) <- favorite
      JField("User__c", JString(userId)) <- favorite
    } yield Favorite(propertyId, userId)

    val rdd = sc.parallelize(favorites)

    TrainingData(rdd)
  }
}

case class Favorite(propertyId: String, userId: String)

case class TrainingData(favorites: RDD[Favorite])
