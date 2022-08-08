use std::fs::File;
use clap::ArgMatches;
use std::io::prelude::*;
use futures::StreamExt;
use crate::services;

pub async fn find(sub_matches: &ArgMatches) {
  let mut file: Option<Result<File, std::io::Error>> = None;
  if sub_matches.is_present("file") {
    file = Some(File::create(sub_matches.get_one::<String>("file")
    .expect("Specify filename to write proxies to")));
  }

  let mut vecs_of_unchecked_proxies = services::collector::collect();
  while let Some(unchecked_proxies) = vecs_of_unchecked_proxies.next().await {
    let mut list_of_checked_proxies = services::checker::check(unchecked_proxies);
    while let Some(proxy) = list_of_checked_proxies.next().await {
      match proxy {
        Ok(proxy) => {
          if proxy.alive {
            if let Some(ref mut value) = file {
              match &value {
                Ok(file) => {
                  file.clone().write(format!("curl -x {}:{} httpbin.org/ip &\n", proxy.host, proxy.port).as_bytes());
                },
                Err(err) => println!("Could not write to file: {}", err)
              }
            }
            println!("{}:{}", proxy.host, proxy.port);
          }
        },
        Err(_err) => {}
      }
    }
  }
}

// use std::fs::File;
// use clap::ArgMatches;
// use std::io::prelude::*;
// use futures::StreamExt;
// use crate::services;

// pub async fn find(sub_matches: &ArgMatches) {
//   let mut file = File::create("proxies.txt");
//   let mut vecs_of_unchecked_proxies = services::collector::collect();
//   while let Some(unchecked_proxies) = vecs_of_unchecked_proxies.next().await {
//     let mut list_of_checked_proxies = services::checker::check(unchecked_proxies);
//     while let Some(proxy) = list_of_checked_proxies.next().await {
//       match proxy {
//         Ok(proxy) => {
//           if proxy.alive {
//             match file {
//               Ok(ref mut file) => {
//                 file.write(format!("curl -vx {}:{} httpbin.org/ip\n", proxy.host, proxy.port).as_bytes());
//               },
//               Err(ref err) => println!("Could not write to file: {}", err)
//             }
//             println!("{}:{}", proxy.host, proxy.port);
//           }
//         },
//         Err(_err) => {}
//       }
//     }
//   }
// }