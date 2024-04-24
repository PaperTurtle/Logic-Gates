name: Build and Deploy Logic Gates Simulator

on:
   push:
      branches: [master]
   pull_request:
      branches: [master]

jobs:
   build-and-release-jar:
      runs-on: ubuntu-latest

      steps:
         - name: Checkout Repository
           uses: actions/checkout@v3

         - name: Build Docker Image
           run: |
              docker build -t javafx-builder .

         - name: Run Docker Container
           run: |
              docker run it --name javafx-build javafx-builder

         - name: Copy JAR file from Docker Container
           run: |
              docker cp javafx-build:/usr/src/app/target/logic_gates-1.0-SNAPSHOT-shaded.jar .

         - name: Clean up Docker Container
           run: |
              docker rm javafx-build

         - name: Create Release
           id: create_release
           uses: actions/create-release@v1
           env:
              GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
           with:
              tag_name: ${{ github.ref_name }}
              release_name: Release ${{ github.ref_name }} - ${{ github.sha }}
              draft: false
              prerelease: false

         - name: Upload Release Asset
           id: upload-release-asset
           uses: actions/upload-release-asset@v1
           env:
              GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
           with:
              upload_url: ${{ steps.create_release.outputs.upload_url }}
              asset_path: ./logic_gates-1.0-SNAPSHOT-shaded.jar
              asset_name: logic_gates-1.0-SNAPSHOT-shaded.jar
              asset_content_type: application/java-archive
