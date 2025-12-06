<img width="780" height="438" alt="스크린샷 2025-12-06 215052" src="https://github.com/user-attachments/assets/18242c62-185c-4312-899d-c9476009049e" />

<img width="1120" height="670" alt="스크린샷 2025-12-06 215102" src="https://github.com/user-attachments/assets/09014bce-0774-44c8-af6b-5bd834a379af" />
<img width="951" height="329" alt="스크린샷 2025-12-06 215132" src="https://github.com/user-attachments/assets/b7d7d6a0-01a7-4773-b949-e14a3ba4e07a" />

Add it in your settings.gradle.kts at the end of repositories:

	dependencyResolutionManagement {
		repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
		repositories {
			mavenCentral()
			maven { url = uri("https://jitpack.io") } 
		}
	}
Step 2. Add the dependency

	dependencies {
	        implementation("com.github.2mori:ByteSerializer:main")
	}
