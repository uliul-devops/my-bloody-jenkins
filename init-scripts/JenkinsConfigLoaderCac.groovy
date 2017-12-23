import org.yaml.snakeyaml.Yaml
import jenkins.security.ApiTokenProperty
import hudson.model.User

def loadYamlConfig(filename){
    return new File(filename).withReader{
        new Yaml().load(it)
    }
}

def handleConfig(handler, config){
    if(!config){
        println "--> skipping ${handler} configuration"
        return
    }
    println "--> Handling ${handler} configuration"
    try{
        evaluate(new File("/usr/share/jenkins/config-handlers/${handler}Config.groovy")).setup(config)
        println "--> Handling ${handler} configuration... done"
    }catch(e){
        println "--> Handling ${handler} configuration... error: ${e}"
        e.printStackTrace()
    }
}

def getAdminUserName(){
    return System.getenv()['JENKINS_ENV_ADMIN_USER']
}

def storeAdminApiToken(adminUser, filename){
    def adminUserApiToken = User.get(adminUser, true)?.getProperty(ApiTokenProperty)?.apiTokenInsecure
    if(adminUserApiToken){
        new File(filename).withWriter{out -> out.println "${adminUser}:${adminUserApiToken}"}
    }
}

def adminUser = getAdminUserName()
if(!adminUser){
    println "JENKINS_ENV_ADMIN_USER was not set. This is mandatory variable"
}else{
    storeAdminApiToken(adminUser, System.getenv()['TOKEN_FILE_LOCATION'])
}

def configFileName = System.getenv()['CONFIG_FILE_LOCATION']

if(!new File(configFileName).exists()) {
    println "${configFileName} does not exist. Set variable JENKINS_ENV_CONFIG_YAML! Skipping configuration..."
} else {
    def jenkinsConfig = loadYamlConfig(configFileName)
    new File(configFileName).withInputStream{is -> org.jenkinsci.plugins.casc.ConfigurationAsCode.configure(is)}
}