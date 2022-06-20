FROM entando/entando-component-manager:6.3.26

COPY target/entando-component-manager.jar /opt/app.jar
WORKDIR /opt
ENTRYPOINT ["/entrypoint.sh"]
CMD ["java", "-XX:MaxRAMPercentage=80.0", "-jar", "app.jar"]
