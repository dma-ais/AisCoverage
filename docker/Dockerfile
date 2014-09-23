FROM kaspernielsen/javabox

RUN apt-get install maven -y
RUN mkdir -p /data
RUN git clone https://github.com/dma-ais/AisCoverage /AisCoverage
RUN (cd /AisCoverage; mvn clean install)

RUN (mv /AisCoverage/target/ais-coverage*dist/ais* /ais-coverage)


WORKDIR /ais-coverage
ADD ./start.sh /ais-coverage/start.sh

RUN echo '<?xml version="1.0" encoding="UTF-8" standalone="yes"?><aisCoverageConfiguration><aisbus><transformer xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="sourceTypeSatTransformerConfiguration"><gh_region>802</gh_region><gh_region>804</gh_region><gh_region>810</gh_region><source>SOURCE</source></transformer><provider xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="tcpClientProviderConfiguration"><hostPort>PLACEHOLDER</hostPort></provider><consumer xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="distributerConsumerConfiguration"><name>UNFILTERED</name></consumer></aisbus><!--Sets the grid granularity; the latitude and longitude minimum degree --><latSize>0.0225225225</latSize><lonSize>0.0386812541</lonSize><!-- Access the frontend via http://localhost:8090/coverage --><serverConfiguration><port>8090</port><webappPath>web</webappPath><contextPath>/coverage</contextPath></serverConfiguration></aisCoverageConfiguration>' > CONFIG_BASE.xml ;

EXPOSE 8090

ENV SERVER 127.0.0.1
ENV PORT 12345
ENV SOURCE_NAME DEFAULT_SOURCE

CMD ["/bin/bash", "start.sh"]
