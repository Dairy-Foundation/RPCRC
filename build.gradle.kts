plugins {
	id("java-library")
	id("org.jetbrains.kotlin.jvm")
	id("maven-publish")
}

group = "dev.frozenmilk.rpcrc"

java {
	sourceCompatibility = JavaVersion.VERSION_1_8
	targetCompatibility = JavaVersion.VERSION_1_8
}

kotlin {
	compilerOptions {
		freeCompilerArgs.add("-Xjvm-default=all")
	}
}

dependencies {
	testImplementation("org.testng:testng:6.9.6")

	api(project(":Util"))
}

publishing {
	publications {
		register<MavenPublication>("release") {
			groupId = "dev.frozenmilk"
			artifactId = "RPCRC"
			version = "0.0.0"

			afterEvaluate {
				from(components["kotlin"])
			}
		}
	}
	repositories {
		maven {
			name = "RPCRC"
			url = uri("${project.buildDir}/release")
		}
	}
}
