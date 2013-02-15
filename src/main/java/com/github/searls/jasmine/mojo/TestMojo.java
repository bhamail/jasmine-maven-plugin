package com.github.searls.jasmine;

import com.github.searls.jasmine.format.JasmineResultLogger;
import com.github.searls.jasmine.io.scripts.TargetDirScriptResolver;
import com.github.searls.jasmine.model.JasmineResult;
import com.github.searls.jasmine.runner.ReporterType;
import com.github.searls.jasmine.runner.SpecRunnerExecutor;
import com.github.searls.jasmine.runner.SpecRunnerHtmlGenerator;
import com.github.searls.jasmine.runner.SpecRunnerHtmlGeneratorFactory;
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoFailureException;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

/**
 * @component
 * @goal test
 * @phase test
 * @execute phase="jasmine-process-test-resources"
 */
public class TestMojo extends AbstractJasmineMojo {

  public void run() throws Exception {
    if(!skipTests) {
      getLog().info("Executing Jasmine Specs");
      File runnerFile = writeSpecRunnerToOutputDirectory();
      JasmineResult result = executeSpecs(runnerFile);
      logResults(result);
      throwAnySpecFailures(result);
    } else {
      getLog().info("Skipping Jasmine Specs");
    }
  }

  private File writeSpecRunnerToOutputDirectory() throws IOException {

    SpecRunnerHtmlGenerator generator = new SpecRunnerHtmlGeneratorFactory().create(ReporterType.JsApiReporter, this, new TargetDirScriptResolver(this));

    String html = generator.generate();

    getLog().debug("Writing out Spec Runner HTML " + html + " to directory " + jasmineTargetDir);
    File runnerFile = new File(jasmineTargetDir,specRunnerHtmlFileName);
    FileUtils.writeStringToFile(runnerFile, html);
    return runnerFile;
  }

  private JasmineResult executeSpecs(File runnerFile) throws MalformedURLException {
    WebDriver driver = createDriver();
    JasmineResult result = new SpecRunnerExecutor().execute(
      runnerFile.toURI().toURL(),
      new File(jasmineTargetDir,junitXmlReportFileName),
      driver,
      timeout, debug, getLog(), format);
    return result;
  }

  private WebDriver createDriver() {
    return new WebDriverConfiguration(webDriverClassName, remoteWebDriverUrl, browserVersion, debug).createWebDriver();
  }

  private void logResults(JasmineResult result) {
    JasmineResultLogger resultLogger = new JasmineResultLogger();
    resultLogger.setLog(getLog());
    resultLogger.log(result);
  }

  private void throwAnySpecFailures(JasmineResult result) throws MojoFailureException {
    if(haltOnFailure && !result.didPass()) {
      throw new MojoFailureException("There were Jasmine spec failures.");
    }
  }


}
