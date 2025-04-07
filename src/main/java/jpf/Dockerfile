FROM openjdk:8
ENV JPF_HOME=/opt/jpf
ENV PATH="$JPF_HOME/bin:$PATH"
WORKDIR /jpf-workspace
COPY JPF /jpf-workspace
COPY ../.. /jpf-workspace/
COPY TestMultiThread.jpf /jpf-workspace/
CMD ["/bin/bash"]